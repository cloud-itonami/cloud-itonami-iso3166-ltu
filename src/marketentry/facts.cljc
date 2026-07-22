(ns marketentry.facts
  "Lithuania market-entry catalog -- the honest, non-fabricating
  source of truth `marketentry.governor` and `marketentry.marketentryllm`
  cite against. Every field traces to an independently-verified fact;
  an item not in `catalog` has NO spec-basis, full stop.

  Verified facts (LTU):
    - business registration: Registrų centras (State Enterprise Centre
      of Registers), a state enterprise under the Ministry of Economy
      and Innovation est. 1997, maintains the legal-entities register
      (Juridinių asmenų registras) among 11 national registers.
      Portal: registrucentras.lt.
    - public procurement law: Viešųjų pirkimų įstatymas (Law on Public
      Procurement, No. I-1491, as amended) -- the primary classical-
      sector procurement statute transposing EU directives. Regulator/
      oversight: Viešųjų pirkimų tarnyba (VPT) -- Public Procurement
      Office.
    - e-procurement platform: CVP IS (Centrinė viešųjų pirkimų
      informacinė sistema), run/administered by VPT, mandatory for
      essentially all procurement above and below EU thresholds. Since
      2024-12-01, ALL public procurement runs on the NEW CVP IS at
      viesiejipirkimai.lt; the OLD system (pirkimai.eviesiejipirkimai.lt
      / eviesiejipirkimai.lt) now serves ONLY historical/archive lookups
      -- see `legacy-cvp-is-domains` / `stale-cvp-is-domain?` below, and
      `marketentry.governor`'s `stale-cvp-is-domain` HARD check, which
      is this vertical's flagship new check (grep-verified absent as a
      governor check function name fleet-wide at build time).
    - tax registration: Valstybinė mokesčių inspekcija (VMI) -- State
      Tax Inspectorate, an agency under the Ministry of Finance,
      administers VAT registration via its EDS electronic-declaration
      system. VAT number format: LT + taxpayer number + VAT index \"1\"
      + check digit. VAT registration threshold: EUR 45,000 annual
      taxable turnover (12-month window) for Lithuanian-established
      businesses.

  Explicitly NOT claimed: no precise \"as amended by Law No. X-YYYY of
  [date]\" citation for I-1491 (not independently verified -- the
  catalog cites only \"as amended\", never a fabricated amendment
  chain)."
  (:require [clojure.string :as str]))

(def legacy-cvp-is-domains
  "CVP IS domains that were the LIVE procurement platform before the
  2024-12-01 migration and now serve ONLY historical/archive lookups.
  Citing either of these as a LIVE provenance URL is staleness, not a
  valid spec-basis."
  #{"eviesiejipirkimai.lt" "pirkimai.eviesiejipirkimai.lt"})

(defn stale-cvp-is-domain?
  "True when `s` is a string that cites a legacy (pre-2024-12-01,
  archive-only) CVP IS domain. Non-string / nil input is never stale
  (nothing to be stale about)."
  [s]
  (boolean (and (string? s)
                (some #(str/includes? s %) legacy-cvp-is-domains))))

(def catalog
  {"LTU" {:name "Lithuania"
          :owner-authority "Viešųjų pirkimų tarnyba (VPT, Public Procurement Office) / CVP IS"
          :legal-basis "Viešųjų pirkimų įstatymas (Law on Public Procurement, No. I-1491, as amended)"
          :national-spec "CVP IS (Centrinė viešųjų pirkimų informacinė sistema) at viesiejipirkimai.lt -- mandatory since 2024-12-01; the pre-migration eviesiejipirkimai.lt domain is archive-only"
          :provenance "https://viesiejipirkimai.lt/"
          :required-evidence ["Legal Entities Register (Juridinių asmenų registras) extract -- Registrų centras"
                               "Viešųjų pirkimų įstatymas (Law No. I-1491) / VPT compliance record"
                               "CVP IS registration record (current system: viesiejipirkimai.lt, live since 2024-12-01)"
                               "VMI VAT registration record (LT VAT number, required above EUR 45,000 annual taxable turnover)"]
          :rep-owner-authority "Valstybės įmonė Registrų centras (State Enterprise Centre of Registers)"
          :rep-legal-basis "Registration in the Legal Entities Register (Juridinių asmenų registras)"
          :rep-provenance "https://www.registrucentras.lt/"
          :corporate-number-owner-authority "Valstybinė mokesčių inspekcija (VMI, State Tax Inspectorate)"
          :corporate-number-legal-basis "VAT registration via VMI's EDS electronic-declaration system; VAT number format LT + taxpayer number + VAT index \"1\" + check digit; threshold EUR 45,000 annual taxable turnover (12-month window)"
          :corporate-number-provenance "https://www.vmi.lt/"}})

(defn spec-basis [iso3] (get catalog iso3))

(defn coverage
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s) missing (remove catalog iso3s)]
     {:requested (count iso3s) :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note "R0 catalog seed"})))

(defn required-evidence-satisfied? [iso3 submitted]
  (when-let [{:keys [required-evidence]} (spec-basis iso3)]
    (= (count required-evidence) (count (filter (set submitted) required-evidence)))))

(defn evidence-checklist [iso3] (:required-evidence (spec-basis iso3) []))

(defn rep-spec-basis [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:rep-owner-authority sb)
      (select-keys sb [:rep-owner-authority :rep-legal-basis :rep-provenance]))))

(defn corporate-number-spec-basis [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:corporate-number-owner-authority sb)
      (select-keys sb [:corporate-number-owner-authority :corporate-number-legal-basis :corporate-number-provenance]))))
