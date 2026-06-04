import type { Directive, DirectiveBinding } from 'vue';

interface DragHTMLElement extends HTMLElement {
  __drag_start_x__?: number;
  __drag_start_y__?: number;
  __drag_start_left__?: number;
  __drag_start_top__?: number;
}

export const dragDirective: Directive = {
  mounted(el: DragHTMLElement, binding: DirectiveBinding<boolean | { axis?: 'x' | 'y' | 'both'; boundary?: boolean }>) {
    const options = typeof binding.value === 'object' ? binding.value : {};
    const { axis = 'both', boundary = false } = options;
    const enabled = binding.value !== false;

    if (!enabled) return;

    el.style.position = 'absolute';
    el.style.cursor = 'move';

    const startDrag = (e: MouseEvent) => {
      e.preventDefault();
      el.__drag_start_x__ = e.clientX;
      el.__drag_start_y__ = e.clientY;
      el.__drag_start_left__ = el.offsetLeft;
      el.__drag_start_top__ = el.offsetTop;

      const onMove = (ev: MouseEvent) => {
        let left = (el.__drag_start_left__ || 0) + (ev.clientX - (el.__drag_start_x__ || 0));
        let top = (el.__drag_start_top__ || 0) + (ev.clientY - (el.__drag_start_y__ || 0));

        if (boundary && el.parentElement) {
          const parentRect = el.parentElement.getBoundingClientRect();
          const elRect = el.getBoundingClientRect();
          left = Math.max(0, Math.min(left, parentRect.width - elRect.width));
          top = Math.max(0, Math.min(top, parentRect.height - elRect.height));
        }

        if (axis === 'x' || axis === 'both') {
          el.style.left = left + 'px';
        }
        if (axis === 'y' || axis === 'both') {
          el.style.top = top + 'px';
        }
      };

      const onUp = () => {
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onUp);
      };

      document.addEventListener('mousemove', onMove);
      document.addEventListener('mouseup', onUp);
    };

    el.addEventListener('mousedown', startDrag);
    (el as any).__drag_start__ = startDrag;
  },

  beforeUnmount(el: DragHTMLElement) {
    const startDrag = (el as any).__drag_start__;
    if (startDrag) {
      el.removeEventListener('mousedown', startDrag);
    }
  },
};
