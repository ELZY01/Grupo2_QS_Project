import { useEffect, useRef, useState } from 'react';

// Tempo mínimo em que a animação fica visível, para não piscar quando o backend é rápido.
export const MIN_FORMING_MS = 2000;

/**
 * Mantém `true` durante pelo menos `minMs` depois de `active` ficar true,
 * mesmo que o pedido acabe antes.
 *
 * Uso: const showForming = useMinimumDuration(isGenerating);
 */
export function useMinimumDuration(active, minMs = MIN_FORMING_MS) {
  const [visible, setVisible] = useState(active);
  const startedAt = useRef(active ? Date.now() : null);

  useEffect(() => {
    if (active) {
      startedAt.current = Date.now();
      setVisible(true);
      return undefined;
    }
    if (startedAt.current == null) return undefined;
    const remaining = Math.max(0, minMs - (Date.now() - startedAt.current));
    const timer = setTimeout(() => {
      startedAt.current = null;
      setVisible(false);
    }, remaining);
    return () => clearTimeout(timer);
  }, [active, minMs]);

  return visible;
}
