
CREATE TABLE public.sound_sessions (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  name TEXT NOT NULL,
  source TEXT NOT NULL DEFAULT 'geral',
  bands JSONB NOT NULL DEFAULT '[]'::jsonb,
  rms REAL NOT NULL DEFAULT 0,
  peak REAL NOT NULL DEFAULT 0,
  recommendations TEXT,
  notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE public.sound_sessions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Public read sound_sessions"
  ON public.sound_sessions FOR SELECT
  USING (true);

CREATE POLICY "Public insert sound_sessions"
  ON public.sound_sessions FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Public delete sound_sessions"
  ON public.sound_sessions FOR DELETE
  USING (true);

CREATE INDEX idx_sound_sessions_created_at ON public.sound_sessions(created_at DESC);
