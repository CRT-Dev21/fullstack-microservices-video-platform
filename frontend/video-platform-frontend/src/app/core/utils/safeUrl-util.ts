import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

export function buildSafeUrl(
  sanitizer: DomSanitizer,
  base: string,
  path: string | null
): SafeResourceUrl {
  const url = path ? `${base}?path=${encodeURIComponent(path)}` : base;
  return sanitizer.bypassSecurityTrustResourceUrl(url);
}
