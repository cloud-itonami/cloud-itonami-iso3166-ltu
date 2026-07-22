# cloud-itonami-iso3166-ltu

**`:implemented`** for **LTU**. Flagship checks: `stale-cvp-is-domain`
(rejects any proposal citing the legacy, archive-only CVP IS domain),
`vat-registration-missing` (EUR 45,000 VMI threshold).

```
clojure -M:dev:test
```

Open ISO 3166 Blueprint for **LTU**: Lithuania.

This repository designs a forkable OSS business for an independent
public-sector market-entry consultant: an already-incorporated operator
(e.g. a `cloud-itonami-cofog-{code}`, `cloud-itonami-isco-{code}`,
`cloud-itonami-unspsc-{segment}` or `cloud-itonami-{ISIC}` blueprint
fork) gets a Compliance Advisor + independent **Market-Entry Compliance
Governor** to navigate public-procurement registration, local business/
tax registration, and EU single-market rules in Lithuania, so the
operator can win and service a government contract without hiring a
full in-house compliance department.

## Compliance checks (`src/marketentry/governor.cljc`)

Seven HARD checks (a human approver cannot override them) plus a SOFT
confidence/actuation gate. Every regulatory field traces to
`src/marketentry/facts.cljc`, which cites only independently-verified
sources -- see the `facts.cljc` docstring for the full source list.

| # | Check (`:rule`) | Applies to | Grounded in |
|---|---|---|---|
| 1 | `no-spec-basis` | assess / draft / submit | a proposal that cites no official source at all |
| 2 | `evidence-incomplete` | draft / submit | the jurisdiction's required-evidence checklist (Registrų centras / VPĮ-VPT / CVP IS / VMI-VAT) not on file |
| 3 | `stale-cvp-is-domain` **(flagship)** | assess / draft / submit | CVP IS (Centrinė viešųjų pirkimų informacinė sistema), run by Viešųjų pirkimų tarnyba (VPT); the mandatory platform migrated to `viesiejipirkimai.lt` on 2024-12-01, and the old `eviesiejipirkimai.lt` / `pirkimai.eviesiejipirkimai.lt` domain now serves archive/historical lookups ONLY. A proposal citing the legacy domain as live provenance is a HARD hold -- this is this vertical's genuinely new check, and exactly the kind of stale-URL trap this fleet must avoid |
| 4 | `vat-registration-missing` **(flagship)** | submit | Valstybinė mokesčių inspekcija (VMI, State Tax Inspectorate) administers VAT registration via its EDS system; required above EUR 45,000 annual taxable turnover (12-month window). Independently re-checked against the engagement's own `:requires-vat-registration?` ground truth |
| 5 | `engagement-fee-mismatch` | submit | independent recompute of `base-fee + monthly-rate × monitoring-months` against the engagement's own claimed fee |
| 6 | `already-drafted` / `already-submitted` | draft / submit | double-actuation guard on dedicated `:drafted?`/`:submitted?` booleans (never a `:status` value) |
| 7 | confidence floor / actuation gate | all | LLM confidence < 0.6, OR the op is `:filing/draft`/`:filing/submit` (a REAL act) -> escalate to a human |

Sources cited per check:

- **Business registration** (checks 1-2 evidence item #1): Registrų
  centras (State Enterprise Centre of Registers), a state enterprise
  under the Ministry of Economy and Innovation est. 1997, maintains the
  Legal Entities Register (Juridinių asmenų registras) among 11
  national registers. `https://www.registrucentras.lt/`
- **Public procurement law** (checks 1-2 evidence item #2): Viešųjų
  pirkimų įstatymas (Law on Public Procurement, No. I-1491, as
  amended), the primary classical-sector procurement statute
  transposing EU directives; regulator/oversight is Viešųjų pirkimų
  tarnyba (VPT) -- Public Procurement Office.
- **E-procurement platform** (checks 1-2-3): CVP IS
  (Centrinė viešųjų pirkimų informacinė sistema), run/administered by
  VPT, mandatory for essentially all procurement above and below EU
  thresholds. **Since 2024-12-01, the live system is at
  `viesiejipirkimai.lt`; the old `eviesiejipirkimai.lt` /
  `pirkimai.eviesiejipirkimai.lt` domain is archive-only.**
  `https://viesiejipirkimai.lt/`
- **Tax / VAT registration** (checks 1-2-4): Valstybinė mokesčių
  inspekcija (VMI), an agency under the Ministry of Finance,
  administers VAT registration via its EDS electronic-declaration
  system. VAT number format: `LT` + taxpayer number + VAT index `1` +
  check digit. Registration threshold: EUR 45,000 annual taxable
  turnover (12-month window). `https://www.vmi.lt/`

Not independently verified and therefore never claimed: a precise "as
amended by Law No. X-YYYY of [date]" citation chain for I-1491 --
`facts.cljc` cites only "as amended".

## Actuation

`:filing/draft` (preparing a CVP IS registration package) and
`:filing/submit` (actually submitting it) are the two real-world acts
this actor performs. Both are gated by TWO independent layers that
must separately agree before anything reaches a real portal:

1. **`marketentry.governor`** treats `:actuation/draft-filing` /
   `:actuation/submit-filing` as `high-stakes` -- always escalates to
   a human, even when every HARD check is clean.
2. **`marketentry.phase`** never includes `:filing/draft` /
   `:filing/submit` in ANY phase's `:auto` set (phases 0-3) -- a
   permanent structural fact, not a rollout milestone still to come.

`interrupt-before #{:request-approval}` (`src/marketentry/operation.cljc`)
pauses the langgraph-clj StateGraph run at that node; only an explicit
`{:approval {:status :approved :by ...}}` resume can advance it to
`:commit`. Every commit AND every hold appends exactly one fact to the
append-only ledger (`marketentry.store/append-ledger!`) -- nothing is
ever mutated or removed from the ledger, only appended to.

## No robotics premise — digital/data service exemption

Market-entry and procurement-compliance navigation is a pure data/software
service with no physical-domain work (portal registration, document
checklists, regulatory-change monitoring) — the same exemption class as
`cloud-itonami-6310` (HR SaaS replacement) and `cloud-itonami-gtin-*`.
`blueprint.edn` sets `:itonami.blueprint/robotics false` and
`:required-technologies` lists only real capabilities (`:identity`,
`:forms`, `:dmn`, `:bpmn`, `:audit-ledger`), no `:robotics`.

## Core Contract

```text
operator intake + prior filing history
        |
        v
Compliance Advisor -> Market-Entry Compliance Governor -> filing draft, or human sign-off
        |
        v
gated portal registration / filing submission + audit ledger
```

No automated proposal can submit a portal registration or filing the
governor refuses, suppress a compliance record, or claim a legal/tax
conclusion the governor has not cleared. `:filing/submit` is never in any
phase's `:auto` set — it always requires human sign-off (mirrors
`cloud-itonami-M6910`'s `filing-submit-never-auto-at-any-phase`
invariant).

## What this is NOT

- **Not the government of Lithuania.** See
  [`docs/business-model.md`](docs/business-model.md) for the boundary with
  `com-etzhayyim-ooyake` (read-only civic mirror), `matsurigoto` (sovereign
  statecraft), `com-etzhayyim-toritsugi` (individual citizen concierge),
  `legal-entity.etzhayyim.com` (read-only data aggregation), and
  `cloud-itonami-M6910` (company incorporation — a different regulatory
  phase this blueprint assumes is already complete).
- **Not legal or tax advice.** Every regulatory claim must cite the
  official source and route final filings to Lithuanian-licensed counsel
  or a registered agent where the law requires licensed representation.

## Capability layer

Resolves via [`kotoba-lang/iso3166`](https://github.com/kotoba-lang/iso3166)
(ISO 3166 `LTU`). Required capabilities:

- :identity
- :forms
- :dmn
- :bpmn
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.

## Culture catalog

Alongside the market-entry / statute catalogs, this repo carries a
**country-level regional-culture catalog** (ADR-2607171400 addendum 2,
`cloud-itonami-municipality-culture-catalog` Wave 1, in
`com-junkawasaki/root`) — national dishes, protected products, beverages,
crafts, festivals and heritage sites for Lithuania:

- `src/culture/facts.cljc` — the catalog, source of truth (keyed by
  uppercase ISO3, mirroring `statute.facts`).
- `schema/culture.edn` — DataScript schema.
- `data/culture-tx.edn` — derived DataScript tx-data (regenerated from
  the catalog, never hand-edited).

City-level counterparts live in the `cloud-itonami-municipality-*` repos.
Same provenance discipline as the compliance catalogs: every entry cites a
source URL that was actually fetched and read on `:culture/retrieved-at`;
summaries state only what the cited source confirms. An item not in
`culture.facts/catalog` has no spec-basis — never fabricate one.
