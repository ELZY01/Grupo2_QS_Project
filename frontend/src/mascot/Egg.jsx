import Mascot from './Mascot.jsx';
import { EGG_STAGES } from './mascot.js';

/**
 * Ovo da mascote. Abana mais à medida que racha.
 */
export default function Egg({ stage = 0, size = 120, label }) {
  const s = Math.max(0, Math.min(4, stage));
  return (
    <div className={`egg egg-stage-${s}`}>
      <Mascot
        src={EGG_STAGES[s]}
        alt={label ?? `Ovo do Pix, fase ${s + 1} de 5`}
        size={size}
        animated={false}
      />
    </div>
  );
}
