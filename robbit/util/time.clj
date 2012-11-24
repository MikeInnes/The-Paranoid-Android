(ns util.time
  "Thin wrapper for clj-time which works with
  strings instead of java objects."
  (:require [clj-time.core   :as time]
            [clj-time.format :as fmt]))

(def date-format (fmt/formatter "yyyy-MM-dd HH:mm:ss"))

(defn joda->str [date] (fmt/unparse date-format date))

(defn str->joda [date] (fmt/parse date-format date))

(defn secs->date
  "Turns a date in seconds (string/number) and creates a
  DateTime object for that date."
  [seconds]
  (joda->str (time/plus (time/epoch) (-> seconds str read-string time/secs))))

(defn now [] (joda->str (time/now)))

(defn after? [d1 d2] (time/after? (str->joda d1) (str->joda d2)))
