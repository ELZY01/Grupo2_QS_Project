import './Mascot.css';

/**
 * Desenha a mascote em pixel art, sem suavizar os píxeis.
 */
export default function Mascot({ src, alt, size = 120, animated = true }) {
  return (
    <img
      className={`mascot${animated ? ' mascot-animated' : ''}`}
      src={src}
      alt={alt}
      width={size}
      height={Math.round((size * 60) / 53)}
    />
  );
}
