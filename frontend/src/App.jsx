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

function buildInitialStages() {
  return STAGE_NAMES.map((name, index) => ({
    name,
    status: index < 3 ? 'success' : index === 3 ? 'running' : 'pending',
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
      } catch (error) {
        setHealthState({
          status: 'error',
          message: error.message || `Unable to reach ${BACKEND_URL}/health`,
          lastChecked: new Date().toLocaleTimeString(),
        });
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
      animatePipeline();
    } catch (error) {
      setCommitState({
        status: 'error',
        message: error.message || 'Commit request could not be completed.',
      });
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
    <main className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 px-4 py-10 text-slate-100 sm:px-6 lg:px-8">
      <div className="mx-auto flex max-w-7xl flex-col gap-8">
        <section className="overflow-hidden rounded-3xl border border-slate-800 bg-slate-900/80 p-8 shadow-glow backdrop-blur">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.3em] text-cyan-300">
                DevOps dashboard
              </p>
              <h1 className="mt-3 text-4xl font-bold tracking-tight text-white sm:text-5xl">
                React + Vite pipeline console
              </h1>
              <p className="mt-4 max-w-3xl text-sm leading-6 text-slate-300 sm:text-base">
                A lightweight frontend that checks backend health, sends commits to the
                Java service, and visualizes the seven pipeline stages from checkout to
                Docker.
              </p>
            </div>
            <div className="rounded-2xl border border-slate-700 bg-slate-950/70 px-4 py-3 text-sm text-slate-300">
              <div className="font-medium text-slate-100">Live stage summary</div>
              <div className="mt-1">{stageSummary}</div>
            </div>
          </div>
        </section>

        <section className="grid gap-6 xl:grid-cols-[1.1fr_1.1fr_1.2fr]">
          <article className="rounded-3xl border border-slate-800 bg-slate-900/80 p-6 shadow-glow">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-sm font-semibold uppercase tracking-[0.2em] text-cyan-300">
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
          </article>

          <article className="rounded-3xl border border-slate-800 bg-slate-900/80 p-6 shadow-glow">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-cyan-300">
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
                  className="w-full rounded-2xl border border-slate-700 bg-slate-950/80 px-4 py-3 font-mono text-sm text-slate-100 outline-none transition focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/30"
                  placeholder="Paste your code here"
                />
              </label>

              <button
                type="submit"
                disabled={commitState.status === 'submitting'}
                className="inline-flex w-full items-center justify-center rounded-2xl bg-cyan-400 px-5 py-3 font-semibold text-slate-950 transition hover:bg-cyan-300 disabled:cursor-not-allowed disabled:bg-slate-600 disabled:text-slate-300"
              >
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

          <article className="rounded-3xl border border-slate-800 bg-slate-900/80 p-6 shadow-glow">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-cyan-300">
                Pipeline stages
              </p>
              <h2 className="mt-2 text-2xl font-semibold text-white">7-stage visualizer</h2>
              <p className="mt-2 text-sm leading-6 text-slate-300">
                Checkout, Build, Unit Tests, Package, Start App, Selenium, and Docker all
                show their own status color.
              </p>
            </div>

            <div className="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-1 xl:grid-cols-2">
              {stages.map((stage, index) => {
                const meta = STAGE_META[stage.status] ?? STAGE_META.pending;

                return (
                  <div
                    key={stage.name}
                    className={`rounded-2xl border p-4 transition ${meta.card}`}
                  >
                    <div className="flex items-center justify-between gap-3">
                      <div className="flex items-center gap-3">
                        <span className={`h-3 w-3 rounded-full ${meta.dot}`} />
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

        <section className="rounded-3xl border border-slate-800 bg-slate-900/80 p-6 shadow-glow">
          <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-cyan-300">
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
              <div key={endpoint} className="rounded-2xl border border-slate-800 bg-slate-950/70 p-5">
                <p className="text-sm text-slate-400">{endpoint}</p>
                <p className="mt-3 text-4xl font-bold text-white">
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