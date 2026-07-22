(ns marketentry.registry-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.registry :as registry]))

(deftest engagement-fee-recompute
  (let [e {:base-fee 400000 :monthly-rate 25000 :monitoring-months 12 :claimed-fee 700000.0}]
    (is (== 700000.0 (registry/compute-engagement-fee e)))
    (is (true? (registry/engagement-fee-matches-claim? e))))
  (let [bad {:base-fee 400000 :monthly-rate 25000 :monitoring-months 12 :claimed-fee 999000.0}]
    (is (false? (registry/engagement-fee-matches-claim? bad)))))

(deftest register-draft-and-submit
  (let [d (registry/register-draft "eng-1" "LTU" 0)
        s (registry/register-submit "eng-1" "LTU" 0)]
    (is (= "LTU-DFT-000000" (get d "draft_number")))
    (is (= "LTU-SUB-000000" (get s "submit_number")))
    (is (nil? (get-in d ["certificate" "proof"])))
    (is (= "draft-unsigned" (get-in s ["certificate" "status"])))))

(deftest register-requires-ids
  (is (thrown? Exception (registry/register-draft "" "LTU" 0)))
  (is (thrown? Exception (registry/register-submit "eng-1" "" 0))))
