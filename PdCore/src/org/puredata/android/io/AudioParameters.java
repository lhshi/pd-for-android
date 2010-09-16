/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * a class that sniffs out available audio parameters
 * 
 */

package org.puredata.android.io;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;


public class AudioParameters {

	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final int COMMON_RATE = 8000;
	private static final int MAX_CHANNELS = 256;
	private static int sampleRate = 0, inputChannels = 0, outputChannels = 0;
	private static float bufsizeMillis = 100.0f; // conservative choice...

	static {
		init();
	}

	/**
	 * @return a reasonable sample rate that the device supports, 0 if audio output is unavailable
	 */
	public static int suggestSampleRate() { return sampleRate; }
	
	/**
	 * @return the largest number of input channels that the device supports
	 */
	public static int suggestInputChannels() { return inputChannels; }
	
	/**
	 * @return the largest number of output channels that the device supports
	 */
	public static int suggestOutputChannels() { return outputChannels; }
	
	/**
	 * @return suggested buffer size in milliseconds; suggests 100ms, i.e., a fairly conservative choice
	 */
	public static float suggestBufferSizeMillis() { return bufsizeMillis; }

	/**
	 * @param srate sample rate
	 * @param nin   number of input channels
	 * @param nout  number of output channels
	 * @return true if and only if the device supports the given set of parameters
	 */
	public static boolean checkParameters(int srate, int nin, int nout) {
		return inOkay(srate, nin) && outOkay(srate, nout);
	}

	private static void init() {
		for (int n = 1; n < MAX_CHANNELS; n++) {
			if (outOkay(COMMON_RATE, n)) outputChannels = n;
		}
		if (outputChannels == 0) return; // no audio output found; give up
		for (int n = 0; n < 256; n++) {
			if (inOkay(COMMON_RATE, n)) inputChannels = n;
		}
		sampleRate = COMMON_RATE;
		for (int sr: new int[] {11025, 16000, 22050, 32000, 48000, 44100}) {  // make 44100 default, if possible
			if (checkParameters(sr, inputChannels, outputChannels)) sampleRate = sr;
		}
	}

	private static boolean inOkay(int srate, int nin) {
		try {
			return nin == 0 || AudioRecord.getMinBufferSize(srate, VersionedAudioFormat.getInFormat(nin), ENCODING) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean outOkay(int srate, int nout) {
		try {
			return AudioTrack.getMinBufferSize(srate, VersionedAudioFormat.getOutFormat(nout), ENCODING) > 0;
		} catch (Exception e) {
			return false;
		}
	}
}
