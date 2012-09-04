(ns bots
  (:require [monger.core :as mg]
            [postal.core :as postal]
             robbit)
  (:use scp askreddit))

(defn sendmail [msg]
  (try
    (postal/send-message {:host "smtp.sendgrid.com"
                          :user (System/getenv "SENDGRID_USERNAME")
                          :pass (System/getenv "SENDGRID_PASSWORD")}
                         msg)
    (catch Exception e e)))

(defn mongo-connect! []
  (if-let [url (System/getenv "MONGOHQ_URL")]
    (mg/connect-via-uri! url)
    (do
      (mg/connect!)
      (mg/set-db! (mg/get-db "local")))))

(defn start []
  (robbit/start scp-bot       :scp-bot)
  (robbit/start askreddit-bot :askreddit))
