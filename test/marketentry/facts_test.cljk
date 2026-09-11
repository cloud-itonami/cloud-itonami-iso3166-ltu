(ns marketentry.facts-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.facts :as facts]))

(deftest ltu-has-spec-basis
  (let [sb (facts/spec-basis "LTU")]
    (is (some? sb))
    (is (string? (:provenance sb)))
    (is (seq (:required-evidence sb)))
    (is (= 4 (count (:required-evidence sb))) "the four verified checks: Registrų centras / VPĮ-VPT / CVP IS / VMI-VAT")
    (is (some? (facts/rep-spec-basis "LTU")))
    (is (some? (facts/corporate-number-spec-basis "LTU")))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest required-evidence-satisfied
  (let [sb (facts/spec-basis "LTU")
        all (:required-evidence sb)]
    (is (true? (facts/required-evidence-satisfied? "LTU" all)))
    (is (not (facts/required-evidence-satisfied? "LTU" (take 1 all))))
    (is (nil? (facts/required-evidence-satisfied? "ATL" all)))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["LTU" "USA" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["ATL" "USA"] (:missing-jurisdictions c)))))

(deftest provenance-cites-current-cvp-is-domain-not-legacy
  (testing "the live provenance URL must be the NEW (post-2024-12-01) domain, never the archive-only legacy one"
    (let [sb (facts/spec-basis "LTU")]
      (is (re-find #"viesiejipirkimai\.lt" (:provenance sb)))
      (is (not (facts/stale-cvp-is-domain? (:provenance sb)))))))

(deftest stale-cvp-is-domain-detection
  (testing "the legacy pre-migration domain is correctly flagged as stale"
    (is (true? (facts/stale-cvp-is-domain? "https://pirkimai.eviesiejipirkimai.lt/")))
    (is (true? (facts/stale-cvp-is-domain? "https://eviesiejipirkimai.lt/")))
    (is (false? (facts/stale-cvp-is-domain? "https://viesiejipirkimai.lt/")))
    (is (false? (facts/stale-cvp-is-domain? nil)))
    (is (false? (facts/stale-cvp-is-domain? "Viešųjų pirkimų įstatymas (Law No. I-1491, as amended)")))))
