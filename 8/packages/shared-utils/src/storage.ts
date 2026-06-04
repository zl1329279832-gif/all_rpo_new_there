const PREFIX = 'platform_';

export interface StorageOptions {
  expire?: number;
  encrypt?: boolean;
}

interface StorageData<T> {
  data: T;
  timestamp: number;
  expire?: number;
}

export function setStorage<T>(key: string, value: T, options: StorageOptions = {}): void {
  const storageKey = PREFIX + key;
  const data: StorageData<T> = {
    data: value,
    timestamp: Date.now(),
    expire: options.expire,
  };
  localStorage.setItem(storageKey, JSON.stringify(data));
}

export function getStorage<T>(key: string): T | null {
  const storageKey = PREFIX + key;
  const value = localStorage.getItem(storageKey);
  if (!value) return null;

  try {
    const data: StorageData<T> = JSON.parse(value);
    if (data.expire && Date.now() - data.timestamp > data.expire) {
      removeStorage(key);
      return null;
    }
    return data.data;
  } catch {
    return null;
  }
}

export function removeStorage(key: string): void {
  localStorage.removeItem(PREFIX + key);
}

export function clearStorage(): void {
  Object.keys(localStorage).forEach((key) => {
    if (key.startsWith(PREFIX)) {
      localStorage.removeItem(key);
    }
  });
}

export function setSessionStorage<T>(key: string, value: T): void {
  sessionStorage.setItem(PREFIX + key, JSON.stringify(value));
}

export function getSessionStorage<T>(key: string): T | null {
  const value = sessionStorage.getItem(PREFIX + key);
  if (!value) return null;
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

export function removeSessionStorage(key: string): void {
  sessionStorage.removeItem(PREFIX + key);
}

export function clearSessionStorage(): void {
  sessionStorage.clear();
}
