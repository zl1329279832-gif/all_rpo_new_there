const SECRET_KEY = 'platform-secret-key-2024';

export function base64Encode(str: string): string {
  return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (_, p1) => String.fromCharCode(parseInt(p1, 16))));
}

export function base64Decode(str: string): string {
  return decodeURIComponent(atob(str).split('').map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
}

export function simpleEncrypt(str: string, key: string = SECRET_KEY): string {
  let result = '';
  for (let i = 0; i < str.length; i++) {
    const charCode = str.charCodeAt(i) ^ key.charCodeAt(i % key.length);
    result += String.fromCharCode(charCode);
  }
  return base64Encode(result);
}

export function simpleDecrypt(str: string, key: string = SECRET_KEY): string {
  const decoded = base64Decode(str);
  let result = '';
  for (let i = 0; i < decoded.length; i++) {
    const charCode = decoded.charCodeAt(i) ^ key.charCodeAt(i % key.length);
    result += String.fromCharCode(charCode);
  }
  return result;
}

export function md5(str: string): string {
  let hash = 0;
  if (str.length === 0) return hash.toString(16);
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash;
  }
  return Math.abs(hash).toString(16).padStart(8, '0');
}

export function sha256(str: string): string {
  const encoder = new TextEncoder();
  const data = encoder.encode(str);
  let hash = '';
  for (let i = 0; i < data.length; i++) {
    hash += data[i].toString(16).padStart(2, '0');
  }
  return hash.padStart(64, '0');
}

export function randomString(length: number = 16, chars: string = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'): string {
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

export function randomNumber(length: number = 6): string {
  return randomString(length, '0123456789');
}

export function uuid(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

export function hashPassword(password: string, salt?: string): string {
  const actualSalt = salt || randomString(8);
  const hash = md5(password + actualSalt);
  return `${actualSalt}$${hash}`;
}

export function verifyPassword(password: string, hashed: string): boolean {
  const [salt, hash] = hashed.split('$');
  return md5(password + salt) === hash;
}
