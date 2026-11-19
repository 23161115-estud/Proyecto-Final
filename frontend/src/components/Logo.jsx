export function LogoIcon({ size = 40, bg = '#1D5E4B', fg = '#fff', arrow = '#B02A5B' }) {
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" aria-hidden="true">
      <rect width="64" height="64" rx="16" fill={bg} />
      <rect x="15" y="29" width="34" height="24" rx="3" fill={fg} />
      <g fill={bg}>
        {[33, 44].flatMap((y) => [20, 29, 38].map((x) => <rect key={`${x}-${y}`} x={x} y={y} width="6" height="8" rx="3" />))}
      </g>
      <polyline points="13,24 26,18 34,22 46,12" fill="none" stroke={arrow} strokeWidth="4.5" strokeLinecap="round" strokeLinejoin="round" />
      <polygon points="55,7 50,18 43,10" fill={arrow} />
    </svg>
  );
}
