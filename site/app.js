'use strict';

const DIMENSIONS = ['AUTHENTICITY', 'CREDIBILITY', 'EMPATHY', 'UNIQUENESS'];
const LABELS = {
  AUTHENTICITY: 'Authenticity', CREDIBILITY: 'Credibility',
  EMPATHY: 'Empathy', UNIQUENESS: 'Uniqueness'
};
const SCENARIOS = {
  balanced: {
    initials: 'CC', name: 'Casey Chen', summary: 'Open-source maintainer · 4 sample signals',
    signals: [
      { id: 'project', title: 'Maintains a public sample project', issuer: 'Community peer · synthetic', outcome: 'verified', affects: { AUTHENTICITY: .9, CREDIBILITY: .8, UNIQUENESS: .6 }, why: 'A signed, current sample claim with a trusted demo issuer.' },
      { id: 'mentoring', title: 'Mentors first-time contributors', issuer: 'Peer feedback · synthetic', outcome: 'inferred', confidence: .7, affects: { EMPATHY: .9, CREDIBILITY: .4 }, why: 'A synthetic pattern-based inference, discounted by its 70% confidence.' },
      { id: 'talk', title: 'Presented a workshop on testing', issuer: 'Event record · synthetic', outcome: 'verified', affects: { CREDIBILITY: .8, UNIQUENESS: .5 }, why: 'A second verified sample signal supports credibility and uniqueness.' },
      { id: 'award', title: 'Received a community recognition', issuer: 'Badge record · synthetic', outcome: 'unverifiable', affects: { AUTHENTICITY: .7, EMPATHY: .5 }, why: 'The source is unavailable, so it cannot change values and contributes no confidence.' }
    ]
  },
  expert: {
    initials: 'JR', name: 'Jordan Rivera', summary: 'Distributed-systems researcher · 4 sample signals',
    signals: [
      { id: 'paper', title: 'Published a reproducibility guide', issuer: 'Publication record · synthetic', outcome: 'verified', affects: { AUTHENTICITY: .8, CREDIBILITY: .95, UNIQUENESS: .8 }, why: 'A trusted demo issuer has verified the publication record.' },
      { id: 'review', title: 'Reviews open research artifacts', issuer: 'Colleague report · synthetic', outcome: 'inferred', confidence: .8, affects: { EMPATHY: .65, CREDIBILITY: .7 }, why: 'A synthetic colleague pattern is inferred at 80% confidence.' },
      { id: 'tool', title: 'Built a tool for deterministic builds', issuer: 'Project record · synthetic', outcome: 'verified', affects: { AUTHENTICITY: .9, UNIQUENESS: .95 }, why: 'The demo project record is verified and contributes to authenticity and uniqueness.' },
      { id: 'service', title: 'Served on a review panel', issuer: 'Panel record · synthetic', outcome: 'invalid', affects: { CREDIBILITY: .8, EMPATHY: .7 }, why: 'This sample signal has a revoked attestation, so it is excluded and recorded.' }
    ]
  },
  newcomer: {
    initials: 'AT', name: 'Avery Taylor', summary: 'New community participant · 3 sample signals',
    signals: [
      { id: 'intro', title: 'Shared a first project introduction', issuer: 'Self-authored · synthetic', outcome: 'verified', affects: { AUTHENTICITY: .85, UNIQUENESS: .55 }, why: 'A signed sample claim supports authenticity and uniqueness.' },
      { id: 'welcome', title: 'Welcomed a new member', issuer: 'Peer note · synthetic', outcome: 'unverifiable', affects: { EMPATHY: .9 }, why: 'The synthetic peer note is unavailable: confidence falls, value stays the same.' },
      { id: 'contribution', title: 'Submitted a documentation improvement', issuer: 'Repository record · synthetic', outcome: 'inferred', confidence: .6, affects: { CREDIBILITY: .65, EMPATHY: .5 }, why: 'A recent activity pattern is inferred at 60% confidence.' }
    ]
  }
};
const STATUS_LABELS = { verified: 'Verified', inferred: 'Inferred', unverifiable: 'Unknown', invalid: 'Invalid' };
const STATUS_OPTIONS = [
  ['verified', 'Verified'], ['inferred', 'Inferred'], ['unverifiable', 'Unknown'], ['invalid', 'Invalid']
];

const scenarioSelect = document.querySelector('#scenario');
const dimensionsNode = document.querySelector('#dimensions');
const evidenceNode = document.querySelector('#evidence-list');
const explanationNode = document.querySelector('#explanation');
let activeScenario = 'balanced';

function node(tag, className, text) {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text !== undefined) element.textContent = text;
  return element;
}

function signalOutcome(signal) {
  if (signal.outcome === 'inferred') return { kind: 'Inferred', confidence: signal.confidence };
  if (signal.outcome === 'unverifiable') return { kind: 'Unverifiable' };
  if (signal.outcome === 'invalid') return { kind: 'Invalid' };
  return { kind: 'Verified' };
}

function scoreScenario(scenario) {
  const state = Object.fromEntries(DIMENSIONS.map(d => [d, { numerator: 0, denominator: 0, confidence: 0, contributions: [], unknown: [], excluded: [] }]));
  for (const signal of scenario.signals) {
    for (const [dimension, rawValue] of Object.entries(signal.affects)) {
      const item = state[dimension];
      const outcome = signalOutcome(signal);
      if (outcome.kind === 'Verified') {
        item.numerator += rawValue; item.denominator += 1; item.confidence += 1;
        item.contributions.push(signal.title);
      } else if (outcome.kind === 'Inferred') {
        item.numerator += rawValue * outcome.confidence; item.denominator += outcome.confidence; item.confidence += outcome.confidence;
        item.contributions.push(`${signal.title} (inferred)`);
      } else if (outcome.kind === 'Unverifiable') {
        item.unknown.push(signal.title);
      } else {
        item.excluded.push(signal.title);
      }
    }
  }
  return Object.fromEntries(DIMENSIONS.map(d => [d, {
    value: state[d].denominator ? state[d].numerator / state[d].denominator : 0,
    confidence: Math.min(1, state[d].confidence / 2),
    contributions: state[d].contributions,
    unknown: state[d].unknown,
    excluded: state[d].excluded
  }]));
}

function renderDimensions(scenario) {
  const result = scoreScenario(scenario);
  dimensionsNode.replaceChildren();
  for (const dimension of DIMENSIONS) {
    const score = result[dimension];
    const card = node('article', 'dimension-card');
    const top = node('div', 'dimension-top');
    const name = node('h4', '', LABELS[dimension]);
    const percent = node('span', 'score-value', `${Math.round(score.value * 100)}%`);
    top.append(name, percent);
    const valueLabel = node('div', 'meter-label');
    valueLabel.append(node('span', '', 'Value'), node('span', '', `${Math.round(score.value * 100)} / 100`));
    const valueTrack = node('div', 'meter');
    valueTrack.setAttribute('role', 'img'); valueTrack.setAttribute('aria-label', `${LABELS[dimension]} value ${Math.round(score.value * 100)} percent`);
    const valueFill = node('span', 'meter-fill'); valueFill.style.width = `${score.value * 100}%`; valueTrack.append(valueFill);
    const confidenceLabel = node('div', 'meter-label confidence-label');
    confidenceLabel.append(node('span', '', 'Confidence'), node('span', '', `${Math.round(score.confidence * 100)}%`));
    const confidenceTrack = node('div', 'meter confidence-meter');
    confidenceTrack.setAttribute('role', 'img'); confidenceTrack.setAttribute('aria-label', `${LABELS[dimension]} confidence ${Math.round(score.confidence * 100)} percent`);
    const confidenceFill = node('span', 'meter-fill'); confidenceFill.style.width = `${score.confidence * 100}%`; confidenceTrack.append(confidenceFill);
    const details = node('p', 'dimension-note', score.contributions.length ? `${score.contributions.length} contributing signal${score.contributions.length === 1 ? '' : 's'}` : 'No usable evidence');
    card.append(top, valueLabel, valueTrack, confidenceLabel, confidenceTrack, details);
    dimensionsNode.append(card);
  }
}

function renderEvidence(scenario) {
  evidenceNode.replaceChildren();
  for (const signal of scenario.signals) {
    const row = node('article', 'signal-row');
    const marker = node('span', `signal-marker marker-${signal.outcome}`, signal.outcome === 'verified' ? '✓' : signal.outcome === 'inferred' ? '≈' : signal.outcome === 'invalid' ? '×' : '?');
    marker.setAttribute('aria-hidden', 'true');
    const copy = node('div', 'signal-copy');
    copy.append(node('p', 'signal-title', signal.title), node('p', 'signal-issuer', signal.issuer));
    const controls = node('div', 'signal-controls');
    const status = node('span', `status-label status-${signal.outcome}`, STATUS_LABELS[signal.outcome]);
    const select = document.createElement('select');
    select.className = 'status-select'; select.setAttribute('aria-label', `Set status for ${signal.title}`);
    for (const [value, label] of STATUS_OPTIONS) {
      const option = document.createElement('option'); option.value = value; option.textContent = label;
      option.selected = signal.outcome === value; select.append(option);
    }
    select.addEventListener('change', () => {
      signal.outcome = select.value;
      render();
      const updated = evidenceNode.querySelector(`[data-signal="${CSS.escape(signal.id)}"] select`);
      updated?.focus();
    });
    controls.append(status, select);
    row.dataset.signal = signal.id;
    row.append(marker, copy, controls);
    evidenceNode.append(row);
  }
}

function updateExplanation(scenario) {
  const unverifiable = scenario.signals.filter(s => s.outcome === 'unverifiable').length;
  const invalid = scenario.signals.filter(s => s.outcome === 'invalid').length;
  const inferred = scenario.signals.filter(s => s.outcome === 'inferred').length;
  const verified = scenario.signals.filter(s => s.outcome === 'verified').length;
  const bits = [];
  if (verified) bits.push(`${verified} verified signal${verified === 1 ? '' : 's'} contribute at full configured weight`);
  if (inferred) bits.push(`${inferred} inferred signal${inferred === 1 ? ' is' : 's are'} discounted by confidence`);
  if (unverifiable) bits.push(`${unverifiable} unknown signal${unverifiable === 1 ? ' leaves' : 's leave'} values unchanged and reduce confidence`);
  if (invalid) bits.push(`${invalid} invalid signal${invalid === 1 ? ' is' : 's are'} excluded and recorded`);
  explanationNode.textContent = bits.join('. ') + '.';
}

function render() {
  const scenario = SCENARIOS[activeScenario];
  document.querySelector('#avatar').textContent = scenario.initials;
  document.querySelector('#profile-name').textContent = scenario.name;
  document.querySelector('#profile-summary').textContent = scenario.summary;
  renderDimensions(scenario); renderEvidence(scenario); updateExplanation(scenario);
}

scenarioSelect.addEventListener('change', () => { activeScenario = scenarioSelect.value; render(); });
render();
