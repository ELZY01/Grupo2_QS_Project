/**
 * "Rawwwr!" curto feito com a Web Audio API (sem ficheiros de som).
 * Se o browser não deixar tocar som, não faz nada.
 */
export function playRoar() {
  try {
    const AudioCtx = window.AudioContext || window.webkitAudioContext;
    if (!AudioCtx) return;
    const ctx = new AudioCtx();
    const now = ctx.currentTime;

    const osc = ctx.createOscillator();
    osc.type = 'sawtooth';
    osc.frequency.setValueAtTime(220, now);
    osc.frequency.exponentialRampToValueAtTime(90, now + 0.7);

    // Tremor para soar a rugido pequenino.
    const lfo = ctx.createOscillator();
    const lfoGain = ctx.createGain();
    lfo.frequency.value = 28;
    lfoGain.gain.value = 25;
    lfo.connect(lfoGain).connect(osc.frequency);

    const gain = ctx.createGain();
    gain.gain.setValueAtTime(0.0001, now);
    gain.gain.exponentialRampToValueAtTime(0.25, now + 0.05);
    gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.75);

    osc.connect(gain).connect(ctx.destination);
    osc.start(now);
    lfo.start(now);
    osc.stop(now + 0.8);
    lfo.stop(now + 0.8);
    osc.onended = () => ctx.close();
  } catch {
    // Sem som: a animação chega.
  }
}
