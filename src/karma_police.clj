(ns karma-police
  (:use karma-police.karmadecay)
  (:require [reddit.link :as link]
            [reddit.comment :as comment]
             users))

; (def test-url "http://www.reddit.com/r/WTF/comments/13euma/parenting_youre_doing_it_wrong/")

(defn reposts [url]
  (map link/from-url (repost-urls url)))

(defn top-comment [links]
  (->> links
       (map link/top-comment)
       (filter identity)
       (sort-by :score)
       last))

(defn top-comment-formatted [links]
  (let [c (top-comment links)]
    (str (:body c)
         "\n\n" "*~ " (comment/link (:author c) (:permalink c)) "*")))

(defn count-string [n]
  (condp = n
    1 "once"
    2 "twice"
    3 "thrice"
    (str n " times")))

(defn link-list [links]
  (clojure.string/join "\n"
    (map #(str "* " (comment/link (:title %) (:permalink %))) links)))

(defn link-reply [url]
  (when-let [reposts (-> url reposts seq)]
    {:reply (str (top-comment-formatted reposts) "\n\n"
                 "------------\n\n"
                 (comment/superscript
                   "*This image has been submitted "
                   (comment/link (count-string (count reposts)) (karmadecay-url url))
                   " before. Above is the previous top comment.*"))}))

(def karma-police
  {:handler      (comp link-reply :permalink)
   :user-agent   "Top Comment Bot by /u/one_more_minute"
   :subreddits   "funny"
   :type         :link
   :login        users/top-comment
   :log          (comp println str)
   :debug        :true
  })
