import wave
import struct
import math
import os

def generate_sound(filename, frequency, duration, volume=0.5, sample_rate=44100):
    # Calculate number of frames
    num_frames = int(duration * sample_rate)
    
    # Create WAV file
    with wave.open(filename, 'w') as wav_file:
        # Set parameters
        wav_file.setnchannels(1)  # Mono
        wav_file.setsampwidth(2)  # 2 bytes per sample
        wav_file.setframerate(sample_rate)
        
        # Generate audio data
        for i in range(num_frames):
            value = int(volume * 32767 * math.sin(2 * math.pi * frequency * i / sample_rate))
            data = struct.pack('<h', value)
            wav_file.writeframes(data)

# Create sounds directory if it doesn't exist
os.makedirs('sounds', exist_ok=True)

# Generate different sounds
generate_sound('sounds/steal.wav', 880, 0.5)  # A5 note for steal
generate_sound('sounds/share.wav', 440, 0.5)  # A4 note for share
generate_sound('sounds/lose.wav', 220, 0.5)   # A3 note for lose
generate_sound('sounds/both_steal.wav', 660, 0.5)  # E5 note for both steal 