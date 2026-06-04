import type { Directive, DirectiveBinding } from 'vue';
import { useUserStore } from '@/stores';

export const permissionDirective: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const { value } = binding;
    const userStore = useUserStore();

    if (!value) return;

    const hasPermission = userStore.hasPermission(value);
    const hasRole = userStore.hasRole(value);

    if (!hasPermission && !hasRole) {
      el.parentNode?.removeChild(el);
    }
  },

  updated(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const { value } = binding;
    const userStore = useUserStore();

    if (!value) return;

    const hasPermission = userStore.hasPermission(value);
    const hasRole = userStore.hasRole(value);

    if (!hasPermission && !hasRole) {
      el.style.display = 'none';
    } else {
      el.style.display = '';
    }
  },
};

export function usePermission() {
  const userStore = useUserStore();

  return {
    hasPermission: userStore.hasPermission,
    hasRole: userStore.hasRole,
  };
}
