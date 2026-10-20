import './Mascot.css';
import { MASCOT_HEIGHT, MASCOT_WIDTH } from './mascot.js';

/**
 * Desenha a mascote em pixel art, empilhando as camadas (corpo + acessórios).
 */
export default function Mascot({ layers, src, alt, size = 120, animated = true }) {
  const stack = layers && layers.length > 0 ? layers : [src];
  const width = size;
  const height = Math.round((size * MASCOT_HEIGHT) / MASCOT_WIDTH);

  return (
    <div
      className={`mascot${animated ? ' mascot-animated' : ''}`}
      role="img"
      aria-label={alt}
      style={{ width, height }}
    >
      {stack.map((layer, index) => (
        <img key={`${index}-${layer}`} src={layer} alt="" width={width} height={height} />
      ))}
    </div>
  );
}
