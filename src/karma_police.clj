(ns karma-police
  (:use karma-police.karmadecay reddit reddit.comment)
  (:require users))

; (def test-url "http://www.reddit.com/r/WTF/comments/13euma/parenting_youre_doing_it_wrong/")

(defn reposts [url]
  (map link-from-url (repost-urls url)))

(defn top-comment [links]
  (->> links
       (map first-reply)
       (filter identity)
       (sort-by :score)
       last))

(defn top-comment-formatted [links]
  (if-let [{:keys [body author permalink]} (top-comment links)]
    (str body "\n\n"
         (italic
           (str "~ " (hyperlink author permalink))))))

(defn count-string [n]
  (condp = n
    1 "once"
    2 "twice"
    3 "thrice"
    (str n " times")))

; (defn link-list [links]
;   (clojure.string/join "\n"
;     (map #(str "* " (hyperlink (:title %) (:permalink %))) links)))

(defn link-reply [url]
  (when-let [reposts (-> url reposts seq)]
    (when-let [comment (top-comment-formatted reposts)]
      {:reply (str comment "\n\n"
                   "------------"                  "\n\n"
                   (italic
                     (superscript
                       "This image has been submitted "
                       (hyperlink (count-string (count reposts)) (karmadecay-url url))
                       " before. Above is the previous top comment.")))})))

(def karma-police
  {:handler      (comp link-reply :permalink)
   :user-agent   "Top Comment Bot by /u/one_more_minute"
   :subreddits   "funny"
   :type         :link
   :login        users/top-comment
   :log          (comp println str)
   ; :debug        :true
  })
