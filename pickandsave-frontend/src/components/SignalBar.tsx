import type { PriceStatus } from '../types/api';

const STATUS_META: Record<PriceStatus, { label: string; textClass: string; barClass: string }> = {
  best: { label: '지금이 딱 좋아요', textClass: 'text-drop', barClass: 'bg-drop' },
  normal: { label: '보통이에요', textClass: 'text-signal', barClass: 'bg-signal' },
  high: { label: '좀 비싼 편이에요', textClass: 'text-rise', barClass: 'bg-rise' },
};

interface SignalBarProps {
  status: PriceStatus;
  size?: 'sm' | 'md';
}

export default function SignalBar({ status, size = 'md' }: SignalBarProps) {
  const meta = STATUS_META[status];
  const barWidth = size === 'sm' ? 'w-11' : 'w-16';
  const barHeight = size === 'sm' ? 'h-[5px]' : 'h-1.5';

  return (
    <div className="flex items-center gap-2">
      <div className={`flex ${barWidth} ${barHeight} gap-0.5`}>
        <div className={`flex-1 rounded-full ${status === 'best' ? 'bg-drop' : 'bg-line'}`} />
        <div className={`flex-1 rounded-full ${status === 'normal' ? 'bg-signal' : 'bg-line'}`} />
        <div className={`flex-1 rounded-full ${status === 'high' ? 'bg-rise' : 'bg-line'}`} />
      </div>
      <span className={`text-xs font-semibold ${meta.textClass}`}>{meta.label}</span>
    </div>
  );
}
