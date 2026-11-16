import { useLayoutEffect, useState } from 'react';

// Margem do traço dentro do botão (igual ao padding lateral dos separadores).
const INSET = 12;

/**
 * Posição do traço do separador ativo dentro da barra de navegação.
 * Mede o botão com aria-current="page" sempre que o separador muda ou a janela
 * muda de tamanho; o CSS faz o traço deslizar até à posição nova.
 *
 * @param {{ current: HTMLElement | null }} navRef referência para o <nav>
 * @param {string} activeKey separador ativo
 * @returns {{ left: number, width: number } | null}
 */
export function useTabBar(navRef, activeKey) {
  const [bar, setBar] = useState(null);

  useLayoutEffect(() => {
    const measure = () => {
      const active = navRef.current?.querySelector('[aria-current="page"]');
      if (!active) return;
      setBar({
        left: active.offsetLeft + INSET,
        width: Math.max(0, active.offsetWidth - INSET * 2),
      });
    };
    measure();
    window.addEventListener('resize', measure);
    return () => window.removeEventListener('resize', measure);
  }, [navRef, activeKey]);

  return bar;
}
