(ns karma-police.karmadecay
  (:use net.cgrand.enlive-html))

(defn get-html
  "Turn url into html object."
  [url]
  (html-resource (java.net.URL. url)))

; (def test-url "http://www.reddit.com/r/pics/comments/13edku/a_kitten_in_some_holiday_lights/")

(defn results
  "Take an html object and return a list
  of result elements."
  [html]
  (select html [:tr.result :td.info]))

(defn permalink
  "Extract the link from a result."
  [result]
  (-> (select result [:div.title :a]) first :attrs :href))

(defn valid?
  "KarmaDecay only makes one distinction results and ads:
  the presence of the '99.X% similar' text. This checks
  for that."
  [result]
  (->> (select result [:div.similar]) first text (re-find #"similar")))

(defn karmadecay-url [reddit-url]
  (str "http://www.karmadecay.com/"
       (clojure.string/replace reddit-url "http://" "")))

(defn repost-urls [reddit-url]
  (->> reddit-url karmadecay-url get-html results (filter valid?) (map permalink)))
