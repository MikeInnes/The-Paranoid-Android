(ns reddit.link
  (:use reddit.core))

(defn from-url [url]
  (let [data     (get-parsed url)
        link     (ffirst data)
        comments (second data)]
    (assoc link :replies comments)))

(defn top-comment [link]
	(-> link :replies first))