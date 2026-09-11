(ns marketentry.governor
  "Market-Entry Compliance Governor -- the independent compliance layer
  that earns the MarketEntry-LLM the right to commit. The LLM has no
  notion of Lithuanian procurement law, whether a claimed engagement
  fee actually equals base + months x rate, whether a corporate/VAT
  number has been verified for a filing that requires it, whether it
  just cited the LEGACY (archive-only, pre-2024-12-01) CVP IS domain
  instead of the current one, or when a draft stops being a draft and
  becomes a real-world portal submission, so this MUST be a separate
  system able to *reject* a proposal and fall back to HOLD.

  `:itonami.blueprint/governor` is `:market-entry-compliance-governor`
  (shared family keyword on blueprints; this is the LTU running
  implementation of that governor, structurally identical to the AGO
  reference implementation with Lithuania's own verified facts).

  This blueprint's own text (docs/business-model.md Trust Controls:
  'any actual portal registration or filing submission requires
  Market-Entry Compliance Governor clearance and always escalates to
  human sign-off'; 'a false or fabricated regulatory-requirement claim
  is a HARD hold') names exactly the checks below.

  Seven checks, in priority order, ALL HARD violations: a human
  approver CANNOT override them. The confidence/actuation gate is
  SOFT: it asks a human to look (low confidence / actuation), and the
  human may approve -- but see `marketentry.phase`: for `:stake
  :actuation/draft-filing`/`:actuation/submit-filing` NO phase ever
  allows auto-commit either. Two independent layers agree that
  actuation is always a human call.

    1. Spec-basis                  -- did the jurisdiction proposal cite
                                       an OFFICIAL source
                                       (`marketentry.facts`), or invent
                                       one?
    2. Evidence incomplete         -- for `:filing/draft`/
                                       `:filing/submit`, has the
                                       jurisdiction actually been
                                       assessed with a full evidence
                                       checklist (Registrų centras /
                                       VPĮ-VPT / CVP IS / VMI-VAT) on
                                       file?
    3. Stale CVP IS domain         -- for `:jurisdiction/assess`/
                                       `:filing/draft`/`:filing/submit`,
                                       did the proposal cite the LEGACY
                                       pre-2024-12-01 CVP IS domain
                                       (eviesiejipirkimai.lt /
                                       pirkimai.eviesiejipirkimai.lt,
                                       archive-only since the migration)
                                       instead of the current
                                       viesiejipirkimai.lt? FLAGSHIP
                                       genuinely new check for the
                                       iso3166 family (grep-verified
                                       absent as a governor check
                                       function name fleet-wide at
                                       build time) -- this is exactly
                                       the stale-URL trap this fleet
                                       must avoid, enforced as code
                                       rather than left to prose.
    4. VAT registration missing    -- for `:filing/submit`, when the
                                       engagement declares
                                       `:requires-vat-registration?
                                       true`, INDEPENDENTLY verify
                                       `:vat-registered?` is true.
                                       Grounded in VMI's EUR 45,000
                                       annual-taxable-turnover
                                       threshold. CONDITIONAL on the
                                       engagement's own ground truth.
    5. Engagement fee mismatch     -- for `:filing/submit`,
                                       INDEPENDENTLY recompute whether
                                       the engagement's own `:claimed-
                                       fee` equals `base-fee +
                                       monthly-rate x monitoring-
                                       months` -- honest reapplication
                                       of the ground-truth-recompute
                                       discipline sibling actors use.
    6. Confidence floor / actuation
       gate                          -- LLM confidence below threshold,
                                       OR the op is `:filing/draft`/
                                       `:filing/submit` (REAL acts)
                                       -> escalate.

  Two more guards, double-draft/double-submit prevention, are enforced
  off dedicated `:drafted?`/`:submitted?` facts (never a `:status`
  value)."
  (:require [marketentry.facts :as facts]
            [marketentry.registry :as registry]
            [marketentry.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Stakes grave enough to always require a human, even when clean.
  Drafting a real portal package and submitting a real portal
  registration are the two real-world actuation events this actor
  performs."
  #{:actuation/draft-filing :actuation/submit-filing})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A `:jurisdiction/assess` (or `:filing/draft`/`:filing/submit`)
  proposal with no spec-basis citation is a HARD violation -- never
  invent a jurisdiction's market-entry requirements."
  [{:keys [op]} proposal]
  (when (contains? #{:jurisdiction/assess :filing/draft :filing/submit} op)
    (let [value (:value proposal)]
      (when (or (empty? (:cites proposal))
                (and (contains? value :spec-basis) (nil? (:spec-basis value))))
        [{:rule :no-spec-basis
          :detail "公式spec-basisの引用が無い提案は法域要件として扱えない"}]))))

(defn- evidence-incomplete-violations
  "For `:filing/draft`/`:filing/submit`, the jurisdiction's required
  registration evidence must actually be satisfied."
  [{:keys [op subject]} st]
  (when (contains? #{:filing/draft :filing/submit} op)
    (let [e (store/engagement st subject)
          assessment (store/assessment-of st subject)]
      (when-not (and assessment
                     (facts/required-evidence-satisfied?
                      (:jurisdiction e) (:checklist assessment)))
        [{:rule :evidence-incomplete
          :detail "法域の必要書類(法人登記/VPT・VPĮ/CVP IS登録/VMI VAT登録等)が充足していない状態での提案"}]))))

(defn- stale-cvp-is-domain-violations
  "For `:jurisdiction/assess`/`:filing/draft`/`:filing/submit`, the
  proposal must never cite the LEGACY pre-2024-12-01 CVP IS domain
  (archive-only) as if it were the live procurement platform -- the
  flagship genuinely new check this vertical adds."
  [{:keys [op]} proposal]
  (when (contains? #{:jurisdiction/assess :filing/draft :filing/submit} op)
    (when (some facts/stale-cvp-is-domain? (:cites proposal))
      [{:rule :stale-cvp-is-domain
        :detail "CVP ISの旧ドメイン(eviesiejipirkimai.lt / pirkimai.eviesiejipirkimai.lt)は2024-12-01移行後アーカイブ専用 -- 現行のviesiejipirkimai.ltを引用すること"}])))

(defn- vat-registration-missing-violations
  "For `:filing/submit`, when the engagement declares
  `:requires-vat-registration? true`, INDEPENDENTLY check
  `:vat-registered?` -- CONDITIONAL on the engagement's own ground
  truth, grounded in VMI's EUR 45,000 annual-taxable-turnover
  threshold."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (and (true? (:requires-vat-registration? e))
                 (not (true? (:vat-registered? e))))
        [{:rule :vat-registration-missing
          :detail (str subject " はVMI VAT登録(年間課税売上高 EUR 45,000超で必須)を要するが未登録 -- 提出提案は進められない")}]))))

(defn- engagement-fee-mismatch-violations
  "For `:filing/submit`, INDEPENDENTLY recompute whether the
  engagement's own claimed fee equals base + months x rate."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when-not (registry/engagement-fee-matches-claim? e)
        [{:rule :engagement-fee-mismatch
          :detail (str subject " の申告手数料(" (:claimed-fee e)
                      ")が独立再計算値(" (registry/compute-engagement-fee e) ")と一致しない")}]))))

(defn- already-drafted-violations
  "For `:filing/draft`, refuses to draft the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/draft)
    (when (store/engagement-already-drafted? st subject)
      [{:rule :already-drafted
        :detail (str subject " は既にドラフト済み")}])))

(defn- already-submitted-violations
  "For `:filing/submit`, refuses to submit the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (when (store/engagement-already-submitted? st subject)
      [{:rule :already-submitted
        :detail (str subject " は既に提出済み")}])))

(defn check
  "Censors a MarketEntry-LLM proposal against the governor rules.
  Returns {:ok? bool :violations [..] :confidence c :escalate? bool
  :high-stakes? bool :hard? bool}."
  [request _context proposal st]
  (let [hard (into []
                   (concat (spec-basis-violations request proposal)
                           (evidence-incomplete-violations request st)
                           (stale-cvp-is-domain-violations request proposal)
                           (vat-registration-missing-violations request st)
                           (engagement-fee-mismatch-violations request st)
                           (already-drafted-violations request st)
                           (already-submitted-violations request st)))
        conf (:confidence proposal 0.0)
        low? (< conf confidence-floor)
        stakes? (boolean (high-stakes (:stake proposal)))
        hard? (boolean (seq hard))]
    {:ok?          (and (not hard?) (not low?) (not stakes?))
     :violations   hard
     :confidence   conf
     :hard?        hard?
     :escalate?    (and (not hard?) (or low? stakes?))
     :high-stakes? stakes?}))

(defn hold-fact
  "The audit fact written when a proposal is rejected (HOLD)."
  [request context verdict]
  {:t          :governor-hold
   :op         (:op request)
   :actor      (:actor-id context)
   :subject    (:subject request)
   :disposition :hold
   :basis      (mapv :rule (:violations verdict))
   :violations (:violations verdict)
   :confidence (:confidence verdict)})
