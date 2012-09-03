(ns bots
  (:require [monger.core :as mg]))

(defn mongo-connect! []
  (mg/connect-via-uri! (System/getenv "MONGOHQ_URL")))

(defn start [])