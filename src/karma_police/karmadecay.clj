(ns karma-police.karmadecay
  (:use net.cgrand.enlive-html))

(defn get-html
  "Turn url into html object."
  [url]
  (html-resource (java.net.URL. url)))

; (def test-url "http://www.reddit.com/r/pics/comments/13edku/a_kitten_in_some_holiday_lights/")

(defn karmadecay-url [url]
  (str "http://www.karmadecay.com/"
       (clojure.string/replace url #"https?://" "")))

(defn share-text [html]
  (-> (select html [:textarea#share1]) first text))

(defn extract-links [share-text]
  (->> share-text
       (re-seq #"\n\[.+\]\((.+)\) \|")
       (map second)))

(defn repost-urls [reddit-url]
  (-> reddit-url karmadecay-url get-html share-text extract-links))
