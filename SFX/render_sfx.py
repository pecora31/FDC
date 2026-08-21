import math
import random
import struct
import wave

SR = 44100

def biquad(signal, kind, freq, q):
    w0 = 2 * math.pi * freq / SR
    alpha = math.sin(w0) / (2 * q)
    cos_w0 = math.cos(w0)

    if kind == 'bandpass':
        b0, b1, b2 = alpha, 0.0, -alpha
    elif kind == 'lowpass':
        b0 = (1 - cos_w0) / 2
        b1 = 1 - cos_w0
        b2 = (1 - cos_w0) / 2
    else:
        raise ValueError(kind)

    a0, a1, a2 = 1 + alpha, -2 * cos_w0, 1 - alpha
    b0, b1, b2 = b0 / a0, b1 / a0, b2 / a0
    a1, a2 = a1 / a0, a2 / a0

    out = [0.0] * len(signal)
    x1 = x2 = y1 = y2 = 0.0
    for i, x0 in enumerate(signal):
        y0 = b0 * x0 + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        out[i] = y0
        x2, x1 = x1, x0
        y2, y1 = y1, y0
    return out

def white_noise(duration):
    n = int(SR * duration)
    return [random.uniform(-1, 1) for _ in range(n)]

def exp_ramp_envelope(duration, start, end):
    n = int(SR * duration)
    ratio = end / start
    return [start * (ratio ** (i / n)) for i in range(n)]

def exp_freq_sine(duration, f0, f1):
    n = int(SR * duration)
    ratio = f1 / f0
    out = [0.0] * n
    phase = 0.0
    for i in range(n):
        t = i / n
        freq = f0 * (ratio ** t)
        phase += 2 * math.pi * freq / SR
        out[i] = math.sin(phase)
    return out

def fixed_freq_sine(duration, f0):
    n = int(SR * duration)
    return [math.sin(2 * math.pi * f0 * i / SR) for i in range(n)]

def mix(*layers):
    n = max(len(layer) for layer in layers)
    out = [0.0] * n
    for layer in layers:
        for i, v in enumerate(layer):
            out[i] += v
    return out

def write_wav(path, signal):
    peak = max(1e-9, max(abs(s) for s in signal))
    if peak > 1.0:
        signal = [s / peak for s in signal]
    frames = struct.pack('<%dh' % len(signal), *[int(max(-1.0, min(1.0, s)) * 32767) for s in signal])
    with wave.open(path, 'w') as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(frames)

# --- Snap pitch kept at the user-approved 1500Hz. Thud cut down (less "thock", more "giòn") and
# --- snap tightened/sharpened (shorter, higher-Q) to read as crisper rather than deep. ---
PITCH = 1500       # Do Sang / Tan So Snap
REL_GAIN = 0.35    # Am Luong Nay Len
THUD_GAIN = 0.15   # Do Dam Cao Su - giam manh de bot "thock"

# ============ PRESS ============
snap_noise = biquad(white_noise(0.003), 'bandpass', PITCH, 7.5)
snap_env = exp_ramp_envelope(0.003, 0.95, 0.001)
snap = [n * e for n, e in zip(snap_noise, snap_env)]

thud_osc = exp_freq_sine(0.045, 130, 55)
thud_env = exp_ramp_envelope(0.045, 0.75 * THUD_GAIN, 0.001)
thud = [o * e for o, e in zip(thud_osc, thud_env)]

sub_osc = fixed_freq_sine(0.07, 60)
sub_env = exp_ramp_envelope(0.07, 0.45 * THUD_GAIN, 0.001)
sub = [o * e for o, e in zip(sub_osc, sub_env)]

press = mix(snap, thud, sub)
write_wav('press.wav', press)

# ============ RELEASE ============
REL_PITCH = 700
knock_noise = biquad(white_noise(0.010), 'bandpass', REL_PITCH, 2.2)
knock_noise = biquad(knock_noise, 'lowpass', 1800, 0.707)
knock_env = exp_ramp_envelope(0.010, 0.55 * REL_GAIN, 0.001)
knock = [n * e for n, e in zip(knock_noise, knock_env)]

thunk_osc = exp_freq_sine(0.020, 150, 75)
thunk_env = exp_ramp_envelope(0.020, 0.4 * REL_GAIN, 0.001)
thunk = [o * e for o, e in zip(thunk_osc, thunk_env)]

release = mix(knock, thunk)
write_wav('release.wav', release)

print("done")
