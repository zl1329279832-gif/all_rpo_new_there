export const patterns = {
  phone: /^1[3-9]\d{9}$/,
  email: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
  idCard: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
  password: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,20}$/,
  username: /^[a-zA-Z][a-zA-Z0-9_]{3,19}$/,
  url: /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$/,
  ip: /^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/,
  chinese: /^[\u4e00-\u9fa5]+$/,
  number: /^\d+$/,
  decimal: /^\d+\.\d+$/,
  positive: /^[1-9]\d*$/,
  negative: /^-[1-9]\d*$/,
};

export function isPhone(value: string): boolean {
  return patterns.phone.test(value);
}

export function isEmail(value: string): boolean {
  return patterns.email.test(value);
}

export function isIdCard(value: string): boolean {
  return patterns.idCard.test(value);
}

export function isPassword(value: string): boolean {
  return patterns.password.test(value);
}

export function isUsername(value: string): boolean {
  return patterns.username.test(value);
}

export function isUrl(value: string): boolean {
  return patterns.url.test(value);
}

export function isIp(value: string): boolean {
  return patterns.ip.test(value);
}

export function isChinese(value: string): boolean {
  return patterns.chinese.test(value);
}

export function isEmpty(value: any): boolean {
  if (value === null || value === undefined) return true;
  if (typeof value === 'string') return value.trim() === '';
  if (Array.isArray(value)) return value.length === 0;
  if (value instanceof Map || value instanceof Set) return value.size === 0;
  if (typeof value === 'object') return Object.keys(value).length === 0;
  return false;
}

export function isNotEmpty(value: any): boolean {
  return !isEmpty(value);
}

export function minLength(value: string, min: number): boolean {
  return value.length >= min;
}

export function maxLength(value: string, max: number): boolean {
  return value.length <= max;
}

export function isRange(value: number, min: number, max: number): boolean {
  return value >= min && value <= max;
}

export function validateForm(rules: Record<string, (value: any) => string | null>, data: Record<string, any>): Record<string, string> {
  const errors: Record<string, string> = {};
  for (const [field, validator] of Object.entries(rules)) {
    const error = validator(data[field]);
    if (error) errors[field] = error;
  }
  return errors;
}
