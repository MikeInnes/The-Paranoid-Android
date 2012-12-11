(ns scp
  "Replies to mentions of SCP-wiki articles with links, on /r/scp."
  (:use reddit.format)
  (:require  users
            [clojure.string  :as str]
            [clj-http.client :as http]))

(def marvin-quotes
 ["I think you ought to know I'm feeling very depressed."
  "I'd make a suggestion, but you wouldn't listen. No one ever does."
  ; "I've been talking to the main computer. It hates me."
  "I've calculated your chance of survival, but I don't think you'll like it."
  "I have a million ideas, but, they all point to certain death."
  "Now I've got a headache."
  "Sorry, did I say something wrong? Pardon me for breathing which I never do anyway so I don't know why I bother to say it oh God I'm so depressed."
  "And then of course I've got this terrible pain in all the diodes down my left side."
  "Do you want me to sit in a corner and rust or just fall apart where I'm standing?"
  "The first ten million years were the worst. And the second ten million: they were the worst, too. The third ten million I didn't enjoy at all. After that, I went into a bit of a decline."
  "It gives me a headache just trying to think down to your level."
  "Life. Loathe it or ignore it. You can't like it."
  "Funny, how just when you think life can't possibly get any worse it suddenly does."
  ;; Not actual quotes.
  "I've been talking to the reddit server. It hates me."
  "Here I am, brain the size of a planet, and they ask me to post links. Call that job satisfaction, 'cause I don't. "
  "I would correct your grammar as well, but you wouldn't listen. No one ever does."])

(defn scp-url [n]
  (str "http://scp-wiki.wikidot.com/scp-" n))

(defn scp-link [n]
  (hyperlink (str "SCP-" n) (scp-url n)))

(defn exists? [n]
  (-> n scp-url (http/get {:throw-exceptions false}) :status (not= 404)))

(defn get-nums
  "Detects numbers 000-1999, including extensions."
  [s] (re-seq #"(?i)(?x)                  # Ignore case, comment mode
                (?<! \d               )   # Not preceded by a digit
                1? \d{3}                  # 000 - 1999
                (?: -EX|-ARC|-J|-D    )?  # Optional extensions
                (?= \ |\.|,|;|:|\n|\Z )   # Followed by punctuation (i.e. not a url)
                (?! \.\d              )   # Not followed by a decimal point"
              s))

(defn probably
  "True with probability n."
  [n] (< (rand) n))

(defn scp-reply [text]
  (when-let [nums (->> text get-nums distinct (filter exists?) seq)]
    {:reply (paragraphs
              (str (str/join ", " (map scp-link nums)) ".")
                (cond
                  (> (count nums) 5)
                    (str "You're not even going to click on all of those, are you? "
                         "Brain the size of a planet, and this is what they've got me doing...")
                  (probably 1/10)
                    (rand-nth marvin-quotes)))
     :vote  :up}))

(def scp-bot
  {:handler      (comp scp-reply :body)
   :user-agent   "/r/scp helper by /u/one_more_minute"
   :subreddits   "scp"
   :login        users/marvin
   ; :log          (comp println str)
   :interval     2
   :debug        :true
  })
