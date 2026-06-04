import type { Directive, DirectiveBinding } from 'vue';
import { debounce } from '@platform/shared-utils';

interface DebounceHTMLElement extends HTMLElement {
  __debounce_handler__?: (e: Event) => void;
}

export const debounceDirective: Directive = {
  mounted(el: DebounceHTMLElement, binding: DirectiveBinding<{ handler: (e: Event) => void; delay?: number; event?: string }>) {
    const { handler, delay = 300, event = 'click' } = binding.value;
    const debouncedHandler = debounce(handler, delay);
    el.addEventListener(event, debouncedHandler);
    el.__debounce_handler__ = debouncedHandler;
  },

  beforeUnmount(el: DebounceHTMLElement, binding: DirectiveBinding<{ handler: (e: Event) => void; delay?: number; event?: string }>) {
    const { event = 'click' } = binding.value;
    if (el.__debounce_handler__) {
      el.removeEventListener(event, el.__debounce_handler__);
    }
  },
};
