export const formatDate = (date: Date | string, format: string = 'DD.MM.YYYY'): string => {
  const d = typeof date === 'string' ? new Date(date) : date;
  
  const day = String(d.getDate()).padStart(2, '0');
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const year = d.getFullYear();
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  
  return format
    .replace('DD', day)
    .replace('MM', month)
    .replace('YYYY', String(year))
    .replace('HH', hours)
    .replace('mm', minutes);
};

export const getInitials = (name: string): string => {
  if (!name || name.length === 0) return 'U';
  
  const parts = name.trim().split(' ');
  if (parts.length >= 2) {
    const first = parts[0]?.[0] || '';
    const second = parts[1]?.[0] || '';
    if (first && second) return (first + second).toUpperCase();
  }
  
  return name[0]?.toUpperCase() || 'U';
};

export const formatNumber = (num: number, decimals: number = 0): string => {
  return num.toLocaleString('ru-RU', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  });
};

export const formatPercentage = (value: number): string => {
  return `${Math.round(value)}%`;
};