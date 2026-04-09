import { useEffect, useMemo, useState } from 'react';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8800';
const LIVE_ENDPOINTS = ['/health', '/', '/api/commit'];

function parseEndpointMetrics(text) {
  const counts = Object.fromEntries(LIVE_ENDPOINTS.map((endpoint) => [endpoint, 0]));
  const lineRegex = /^http_requests_total\{([^}]*)\}\s+([0-9.]+)$/;

  text.split('\n').forEach((line) => {
    const match = line.trim().match(lineRegex);
    if (!match) {
      return;
    }

    const labels = match[1];
    const endpointMatch = labels.match(/endpoint="([^"]+)"/);
    if (!endpointMatch) {
      return;
    }

    const endpoint = endpointMatch[1];
    if (Object.prototype.hasOwnProperty.call(counts, endpoint)) {
      counts[endpoint] = Number(match[2]);
    }
  });

  return counts;
}

const STAGE_NAMES = [
  'Checkout',
  'Build',
  'Unit Tests',
  'Package',
  'Start App',
  'Selenium',
  'Docker',
];

const STAGE_META = {
  pending: {
    label: 'Pending',
    dot: 'bg-slate-500',
    card: 'border-slate-800 bg-slate-900/80',
    text: 'text-slate-400',
  },
  running: {
    label: 'Running',
    dot: 'bg-amber-400',
    card: 'border-amber-400/60 bg-amber-400/10',
    text: 'text-amber-200',
  },
  success: {
    label: 'Success',
    dot: 'bg-emerald-400',
    card: 'border-emerald-400/60 bg-emerald-400/10',
    text: 'text-emerald-200',
  },
  failed: {
    label: 'Failed',
    dot: 'bg-rose-400',
    card: 'border-rose-400/60 bg-rose-400/10',
    text: 'text-rose-200',
  },
};

const DEFAULT_SNIPPET = `import { useMemo } from 'react';

export function PipelineBadge() {
  return <div>DevOps Pipeline Working</div>;
}`;

function classNames(...values) {
  return values.filter(Boolean).join(' ');
}

function pushActivity(current, nextEntry) {
  if (current[0]?.text === nextEntry.text && current[0]?.type === nextEntry.type) {
    return current;
  }

  return [nextEntry, ...current].slice(0, 8);
}

function buildInitialStages() {
  return STAGE_NAMES.map((name, index) => ({
    name,
    status: 'pending',
  }));
}

function App() {
  const [healthState, setHealthState] = useState({
    status: 'loading',
    message: 'Checking backend health...',
    lastChecked: null,
  });
  const [commitState, setCommitState] = useState({
    status: 'idle',
    message: 'Submit code to trigger the backend endpoint.',
  });
  const [code, setCode] = useState(DEFAULT_SNIPPET);
  const [stages, setStages] = useState(buildInitialStages());
  const [liveMetrics, setLiveMetrics] = useState({
    counts: Object.fromEntries(LIVE_ENDPOINTS.map((endpoint) => [endpoint, 0])),
    lastUpdated: null,
    status: 'loading',
  });
  const [activity, setActivity] = useState([
    { type: 'info', text: 'Dashboard initialized. Awaiting first health check.' },
  ]);

  const stageSummary = useMemo(() => {
    const counts = stages.reduce(
      (acc, stage) => {
        acc[stage.status] += 1;
        return acc;
      },
      { pending: 0, running: 0, success: 0, failed: 0 },
    );

    return `${counts.success} success · ${counts.running} running · ${counts.pending} pending`;
  }, [stages]);

  const pipelineProgress = useMemo(() => {
    const completed = stages.filter((stage) => stage.status === 'success').length;
    return Math.round((completed / STAGE_NAMES.length) * 100);
  }, [stages]);

  const totalRequests = useMemo(
    () => Object.values(liveMetrics.counts).reduce((sum, value) => sum + value, 0),
    [liveMetrics.counts],
  );

  const metricsStateLabel =
    liveMetrics.status === 'ready'
      ? 'Live'
      : liveMetrics.status === 'error'
        ? 'Unavailable'
        : 'Loading';

  useEffect(() => {
    setStages(buildInitialStages());
  }, []);

  useEffect(() => {
    let intervalId;

    const checkHealth = async () => {
      try {
        const response = await fetch(`${BACKEND_URL}/health`);
        const text = await response.text();

        if (!response.ok || text.trim() !== 'OK') {
          throw new Error(`Unexpected health response: ${text || response.statusText}`);
        }

        setHealthState({
          status: 'healthy',
          message: 'Backend is reachable and returning OK.',
          lastChecked: new Date().toLocaleTimeString(),
        });
        setActivity((current) =>
          pushActivity(current, {
            type: 'success',
            text: 'Health check passed — backend responded with OK.',
          }),
        );
      } catch (error) {
        setHealthState({
          status: 'error',
          message: error.message || `Unable to reach ${BACKEND_URL}/health`,
          lastChecked: new Date().toLocaleTimeString(),
        });
        setActivity((current) =>
          pushActivity(current, {
            type: 'error',
            text: `Health check failed — ${error.message || 'unknown error'}.`,
          }),
        );
      }
    };

    checkHealth();
    intervalId = setInterval(checkHealth, 15000);

    return () => {
      clearInterval(intervalId);
    };
  }, []);

  useEffect(() => {
    let intervalId;

    const refreshMetrics = async () => {
      try {
        const response = await fetch(`${BACKEND_URL}/metrics`);
        const text = await response.text();

        if (!response.ok) {
          throw new Error(`Metrics request failed with status ${response.status}`);
        }

        setLiveMetrics({
          counts: parseEndpointMetrics(text),
          lastUpdated: new Date().toLocaleTimeString(),
          status: 'ready',
        });
      } catch (error) {
        setLiveMetrics((current) => ({
          ...current,
          status: 'error',
          lastUpdated: new Date().toLocaleTimeString(),
        }));
      }
    };

    refreshMetrics();
    intervalId = setInterval(refreshMetrics, 5000);

    return () => {
      clearInterval(intervalId);
    };
  }, []);

  const animatePipeline = () => {
    setStages(buildInitialStages());

    STAGE_NAMES.forEach((_, index) => {
      const baseDelay = index * 750;

      setTimeout(() => {
        setStages((current) =>
          current.map((stage, stageIndex) => ({
            ...stage,
            status:
              stageIndex < index
                ? 'success'
                : stageIndex === index
                  ? 'running'
                  : 'pending',
          })),
        );
      }, baseDelay);

      setTimeout(() => {
        setStages((current) =>
          current.map((stage, stageIndex) => ({
            ...stage,
            status: stageIndex <= index ? 'success' : stage.status,
          })),
        );
      }, baseDelay + 450);
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setCommitState({
      status: 'submitting',
      message: 'Posting the commit payload to the backend...',
    });

    try {
      const body = new URLSearchParams({
        code,
        filename: 'frontend-snippet.js',
        message: 'Commit from Vite frontend dashboard',
      });

      const response = await fetch(`${BACKEND_URL}/api/commit`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
        },
        body: body.toString(),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Commit request failed.');
      }

      setCommitState({
        status: 'success',
        message: data.message || 'Commit accepted and Jenkins was triggered.',
      });
      setActivity((current) =>
        pushActivity(current, {
          type: 'success',
          text: 'Commit payload submitted successfully.',
        }),
      );
      animatePipeline();
    } catch (error) {
      setCommitState({
        status: 'error',
        message: error.message || 'Commit request could not be completed.',
      });
      setActivity((current) =>
        pushActivity(current, {
          type: 'error',
          text: `Commit submission failed — ${error.message || 'request error'}.`,
        }),
      );
      setStages((current) =>
        current.map((stage, index) => ({
          ...stage,
          status: index === 0 ? 'failed' : stage.status,
        })),
      );
    }
  };

  const healthVisual =
    healthState.status === 'healthy'
      ? STAGE_META.success
      : healthState.status === 'error'
        ? STAGE_META.failed
        : STAGE_META.running;

  return (
    <main className="relative min-h-screen overflow-hidden bg-slate-950 px-4 py-10 text-slate-100 sm:px-6 lg:px-8">
      <div className="pointer-events-none absolute -left-32 -top-32 h-80 w-80 rounded-full bg-cyan-500/20 blur-3xl" />
      <div className="pointer-events-none absolute -right-24 top-40 h-72 w-72 rounded-full bg-indigo-500/20 blur-3xl" />
      <div className="pointer-events-none absolute bottom-0 left-1/3 h-64 w-64 rounded-full bg-fuchsia-500/10 blur-3xl" />

      <div className="relative mx-auto flex max-w-7xl flex-col gap-8">
        <section className="overflow-hidden rounded-3xl border border-slate-800/80 bg-slate-900/70 p-8 shadow-glow backdrop-blur-xl">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.35em] text-cyan-300">
                DevOps dashboard
              </p>
              <h1 className="mt-3 text-4xl font-bold tracking-tight text-white sm:text-5xl">
                Modern pipeline control center
              </h1>
              <p className="mt-4 max-w-3xl text-sm leading-6 text-slate-300 sm:text-base">
                Real-time health checks, commit trigger workflow, stage visualization, and
                endpoint telemetry in one clean console.
              </p>
            </div>

            <div className="grid w-full gap-3 sm:grid-cols-3 lg:w-auto lg:min-w-[440px]">
              <div className="rounded-2xl border border-slate-700 bg-slate-950/60 px-4 py-3">
                <div className="text-xs uppercase tracking-widest text-slate-400">Pipeline</div>
                <div className="mt-1 text-lg font-semibold text-white">{pipelineProgress}%</div>
                <div className="mt-1 text-xs text-slate-400">{stageSummary}</div>
              </div>
              <div className="rounded-2xl border border-slate-700 bg-slate-950/60 px-4 py-3">
                <div className="text-xs uppercase tracking-widest text-slate-400">Requests</div>
                <div className="mt-1 text-lg font-semibold text-white">{totalRequests}</div>
                <div className="mt-1 text-xs text-slate-400">Across tracked endpoints</div>
              </div>
              <div className="rounded-2xl border border-slate-700 bg-slate-950/60 px-4 py-3">
                <div className="text-xs uppercase tracking-widest text-slate-400">Backend URL</div>
                <div className="mt-1 truncate text-sm font-medium text-white">{BACKEND_URL}</div>
                <div className="mt-1 text-xs text-slate-400">Environment aware</div>
              </div>
            </div>
          </div>

          <div className="mt-6 h-2 overflow-hidden rounded-full bg-slate-800">
            <div
              className="h-full rounded-full bg-gradient-to-r from-cyan-400 via-blue-500 to-indigo-500 transition-all duration-500"
              style={{ width: `${pipelineProgress}%` }}
            />
          </div>

          <div className="mt-5 flex flex-wrap items-center gap-3 text-xs">
            <span className="rounded-full border border-slate-700 bg-slate-950/60 px-3 py-1 text-slate-300">
              Metrics: {metricsStateLabel}
            </span>
            <a
              className="rounded-full border border-cyan-500/40 bg-cyan-500/10 px-3 py-1 text-cyan-200 transition hover:border-cyan-400/70 hover:bg-cyan-500/20"
              href="http://localhost:3000"
              target="_blank"
              rel="noreferrer"
            >
              Open Frontend
            </a>
            <a
              className="rounded-full border border-slate-700 bg-slate-950/70 px-3 py-1 text-slate-300 transition hover:border-slate-500"
              href="http://localhost:8800/health"
              target="_blank"
              rel="noreferrer"
            >
              Backend Health
            </a>
            <a
              className="rounded-full border border-slate-700 bg-slate-950/70 px-3 py-1 text-slate-300 transition hover:border-slate-500"
              href="http://localhost:9090"
              target="_blank"
              rel="noreferrer"
            >
              Prometheus
            </a>
            <a
              className="rounded-full border border-slate-700 bg-slate-950/70 px-3 py-1 text-slate-300 transition hover:border-slate-500"
              href="http://localhost:3001"
              target="_blank"
              rel="noreferrer"
            >
              Grafana
            </a>
          </div>
        </section>

        <section className="grid gap-6 2xl:grid-cols-[1.05fr_1.1fr_1.1fr]">
          <article className="rounded-3xl border border-slate-800 bg-slate-900/75 p-6 shadow-glow">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-300">
                  Pipeline status
                </p>
                <h2 className="mt-2 text-2xl font-semibold text-white">Backend health</h2>
              </div>
              <span
                className={`rounded-full px-3 py-1 text-xs font-semibold ${healthVisual.card} ${healthVisual.text}`}
              >
                {healthVisual.label}
              </span>
            </div>

            <div className="mt-6 rounded-2xl border border-slate-800 bg-slate-950/70 p-5">
              <div className="flex items-center gap-3">
                <span className={`h-3 w-3 rounded-full ${healthVisual.dot}`} />
                <p className="text-lg font-medium text-white">
                  {healthState.status === 'healthy'
                    ? 'Backend reachable'
                    : healthState.status === 'error'
                      ? 'Backend unavailable'
                      : 'Checking backend'}
                </p>
              </div>
              <p className="mt-3 text-sm leading-6 text-slate-300">{healthState.message}</p>
              <dl className="mt-5 grid gap-4 text-sm sm:grid-cols-2">
                <div className="rounded-2xl border border-slate-800 bg-slate-900/70 p-4">
                  <dt className="text-slate-400">Endpoint</dt>
                  <dd className="mt-1 font-medium text-white">GET /health</dd>
                </div>
                <div className="rounded-2xl border border-slate-800 bg-slate-900/70 p-4">
                  <dt className="text-slate-400">Last checked</dt>
                  <dd className="mt-1 font-medium text-white">
                    {healthState.lastChecked || 'Just now'}
                  </dd>
                </div>
              </dl>
            </div>

            <div className="mt-5 rounded-2xl border border-slate-800 bg-slate-950/60 p-4">
              <p className="text-xs font-semibold uppercase tracking-[0.15em] text-slate-400">
                Recent activity
              </p>
              <ul className="mt-3 space-y-2 text-sm">
                {activity.slice(0, 5).map((entry, index) => (
                  <li key={`${entry.text}-${index}`} className="flex items-start gap-2 text-slate-300">
                    <span
                      className={classNames(
                        'mt-2 h-2 w-2 rounded-full',
                        entry.type === 'success'
                          ? 'bg-emerald-400'
                          : entry.type === 'error'
                            ? 'bg-rose-400'
                            : 'bg-cyan-400',
                      )}
                    />
                    <span>{entry.text}</span>
                  </li>
                ))}
              </ul>
            </div>
          </article>

          <article className="rounded-3xl border border-slate-800 bg-slate-900/75 p-6 shadow-glow">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-300">
                Commit form
              </p>
              <h2 className="mt-2 text-2xl font-semibold text-white">Send a commit payload</h2>
              <p className="mt-2 text-sm leading-6 text-slate-300">
                Type code, submit it, and the frontend posts to the backend commit API.
              </p>
            </div>

            <form onSubmit={handleSubmit} className="mt-6 space-y-4">
              <label className="block">
                <span className="mb-2 block text-sm font-medium text-slate-200">Code</span>
                <textarea
                  value={code}
                  onChange={(event) => setCode(event.target.value)}
                  rows={11}
                  className="w-full rounded-2xl border border-slate-700 bg-slate-950/85 px-4 py-3 font-mono text-sm text-slate-100 outline-none transition focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/30"
                  placeholder="Paste your code here"
                />
              </label>

              <div className="grid gap-3 sm:grid-cols-2">
                <div className="rounded-2xl border border-slate-800 bg-slate-950/60 p-3 text-xs text-slate-300">
                  <span className="text-slate-400">Payload size</span>
                  <p className="mt-1 text-sm font-semibold text-white">{code.length} chars</p>
                </div>
                <div className="rounded-2xl border border-slate-800 bg-slate-950/60 p-3 text-xs text-slate-300">
                  <span className="text-slate-400">Commit target</span>
                  <p className="mt-1 text-sm font-semibold text-white">/api/commit</p>
                </div>
              </div>

              <button
                type="submit"
                disabled={commitState.status === 'submitting'}
                className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-gradient-to-r from-cyan-400 to-blue-500 px-5 py-3 font-semibold text-slate-950 transition hover:from-cyan-300 hover:to-blue-400 disabled:cursor-not-allowed disabled:from-slate-600 disabled:to-slate-600 disabled:text-slate-300"
              >
                <span className="inline-flex h-2.5 w-2.5 rounded-full bg-slate-900/70" />
                {commitState.status === 'submitting' ? 'Submitting...' : 'Submit'}
              </button>

              <div
                className={`rounded-2xl border px-4 py-3 text-sm ${
                  commitState.status === 'success'
                    ? 'border-emerald-500/40 bg-emerald-500/10 text-emerald-200'
                    : commitState.status === 'error'
                      ? 'border-rose-500/40 bg-rose-500/10 text-rose-200'
                      : 'border-slate-800 bg-slate-950/70 text-slate-300'
                }`}
              >
                {commitState.message}
              </div>
            </form>
          </article>

              <article className="rounded-3xl border border-slate-800 bg-slate-900/75 p-6 shadow-glow">
            <div>
                  <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-300">
                Pipeline stages
              </p>
              <h2 className="mt-2 text-2xl font-semibold text-white">7-stage visualizer</h2>
              <p className="mt-2 text-sm leading-6 text-slate-300">
                Checkout, Build, Unit Tests, Package, Start App, Selenium, and Docker all
                show their own status color.
              </p>
            </div>

            <div className="mt-5 h-2 overflow-hidden rounded-full bg-slate-800">
              <div
                className="h-full rounded-full bg-gradient-to-r from-emerald-400 to-cyan-400 transition-all duration-500"
                style={{ width: `${pipelineProgress}%` }}
              />
            </div>

            <div className="mt-6 grid gap-3 sm:grid-cols-2 xl:grid-cols-1 2xl:grid-cols-2">
              {stages.map((stage, index) => {
                const meta = STAGE_META[stage.status] ?? STAGE_META.pending;

                return (
                  <div
                    key={stage.name}
                    className={`group rounded-2xl border p-4 transition hover:-translate-y-0.5 ${meta.card}`}
                  >
                    <div className="flex items-center justify-between gap-3">
                      <div className="flex items-center gap-3">
                        <span className={`h-3 w-3 rounded-full ${meta.dot} ring-4 ring-white/5`} />
                        <div>
                          <p className="font-semibold text-white">{stage.name}</p>
                          <p className={`text-xs ${meta.text}`}>Stage {index + 1}</p>
                        </div>
                      </div>
                      <span
                        className={`rounded-full px-3 py-1 text-xs font-semibold ${meta.card} ${meta.text}`}
                      >
                        {meta.label}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          </article>
        </section>

        <section className="rounded-3xl border border-slate-800 bg-slate-900/75 p-6 shadow-glow">
          <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-300">
                Live Metrics
              </p>
              <h2 className="mt-2 text-2xl font-semibold text-white">API request counts</h2>
            </div>
            <p className="text-sm text-slate-400">
              Source: <span className="text-slate-200">{BACKEND_URL}/metrics</span>
            </p>
          </div>

          <div className="mt-6 grid gap-4 md:grid-cols-3">
            {LIVE_ENDPOINTS.map((endpoint) => (
              <div
                key={endpoint}
                className="rounded-2xl border border-slate-800 bg-slate-950/70 p-5 transition hover:border-cyan-500/40"
              >
                <p className="text-sm font-medium text-slate-300">{endpoint}</p>
                <p className="mt-3 text-4xl font-bold text-white tabular-nums">
                  {liveMetrics.counts[endpoint] ?? 0}
                </p>
                <p className="mt-2 text-xs text-slate-500">
                  {liveMetrics.status === 'error'
                    ? 'Metrics temporarily unavailable'
                    : `Updated ${liveMetrics.lastUpdated || 'just now'}`}
                </p>
              </div>
            ))}
          </div>
        </section>
      </div>
    </main>
  );
}

export default App;