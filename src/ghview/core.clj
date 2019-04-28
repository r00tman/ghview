(ns ghview.core
  (:gen-class)
  (:require [clj-http.client :as client] 
            [progressbar.core :refer [progressbar]]
            [ghview.config :as config])
  )

(def mode 'views)

(defn request
  [what]
  (:body (client/get what {:headers config/auth :as :json}))
  )

(defn traffic
  [repo]
  (assoc (request (apply str [(:url repo) 
                              (if (= mode 'clones) 
                                  "/traffic/clones"
                                  "/traffic/views")])) 
         :name (:name repo))
  )

(defn sum_non_nil
  [what]
  (apply + (filter some? what))
  )

(defn today
  [what]
  (last (if (= mode 'clones) (:clones what) (:views what)))
  )

(defn -main
  [& args]
  (if (some #{"clones"}  args) (def mode 'clones))
  (if (some #{"views"} args) (def mode 'views))
  (def user (request "https://api.github.com/users/r00tman"))

  (println "Fetching repo info...")
  (def repos (request (:repos_url user)))
  (def repos (progressbar 
               repos
               :print-every 1
               :count (count repos)
               :width (min 40 (count repos))
               ))

  (def repos_traffic (map #(traffic %) repos))

  (def total_count (sum_non_nil (map #(:count %) repos_traffic)))
  (def total_uniques (sum_non_nil (map #(:uniques %) repos_traffic)))

  (def today_traffic (map today repos_traffic))
  (def total_today_count (sum_non_nil (map #(:count %) today_traffic)))
  (def total_today_uniques (sum_non_nil (map #(:uniques %) today_traffic)))
  
  (println)
  (def traffic_sorted (sort #(compare (:count %2) (:count %1)) repos_traffic))
  (def table (map (fn [x] {:name (:name x) 
                           :total_count (:count x)
                           :total_uniques (:uniques x) 
                           :today_count (:count (today x))
                           :today_uniques (:uniques (today x))}) 
                  traffic_sorted))

  (def aug_table (cons {:name "Total" 
                        :total_count total_count 
                        :total_uniques total_uniques
                        :today_count total_today_count
                        :today_uniques total_today_uniques}
                       table))
  (clojure.pprint/print-table aug_table)
  )
