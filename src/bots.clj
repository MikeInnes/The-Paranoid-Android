(ns bots
  (:require [monger.core :as mg]
            [postal.core :as postal]
             robbit)
  (:use scp-bot))

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
      (mg/connect!) (mg/set-db! (mg/get-db "local")))))

(defn start []
  (add-watch robbit/log-str :email  (fn [_ _ _ s]
                                      (sendmail {:to   "mike.j.innes@gmail.com"
                                                 :from "bots"
                                                 :subject "bot logs"
                                                 :body s})))
  (robbit/start scp-bot))