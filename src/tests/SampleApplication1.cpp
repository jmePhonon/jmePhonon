#include <algorithm>
#include <fstream>
#include <iterator>
#include <phonon.h>
#include <vector>

std::vector<float> load_input_audio(const std::string filename) {
    std::ifstream file(filename.c_str(), std::ios::binary);
    file.seekg(0, std::ios::end);
    auto filesize = file.tellg();
    auto numsamples = static_cast<int>(filesize / sizeof(float));
    std::vector<float> inputaudio(numsamples);
    file.seekg(0, std::ios::beg);
    file.read(reinterpret_cast<char *>(inputaudio.data()), filesize);
    return inputaudio;
}

void save_output_audio(const std::string filename, std::vector<float> outputaudio) {
    std::ofstream file(filename.c_str(), std::ios::binary);
    file.write(reinterpret_cast<char *>(outputaudio.data()), outputaudio.size() * sizeof(float));
}

int main(int argc, char **argv) {
    auto inputaudio = load_input_audio("resources/inputaudio.raw");
    IPLhandle context{nullptr};
    iplCreateContext(nullptr, nullptr, nullptr, &context);
    auto const samplingrate = 44100;
    auto const framesize = 1024;
    IPLRenderingSettings settings{samplingrate, framesize};
    IPLhandle renderer{nullptr};
    IPLHrtfParams hrtfParams{IPL_HRTFDATABASETYPE_DEFAULT, nullptr, 0, nullptr, nullptr, nullptr};
    iplCreateBinauralRenderer(context, settings, hrtfParams, &renderer);
    IPLAudioFormat mono;
    mono.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
    mono.channelLayout = IPL_CHANNELLAYOUT_MONO;
    mono.channelOrder = IPL_CHANNELORDER_INTERLEAVED;
    IPLAudioFormat stereo;
    stereo.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
    stereo.channelLayout = IPL_CHANNELLAYOUT_STEREO;
    stereo.channelOrder = IPL_CHANNELORDER_INTERLEAVED;
    IPLhandle effect{nullptr};
    iplCreateBinauralEffect(renderer, mono, stereo, &effect);
    std::vector<float> outputaudioframe(2 * framesize);
    std::vector<float> outputaudio;
    IPLAudioBuffer inbuffer{mono, framesize, inputaudio.data()};
    IPLAudioBuffer outbuffer{stereo, framesize, outputaudioframe.data()};
    auto numframes = static_cast<int>(inputaudio.size() / framesize);
    for (auto i = 0; i < numframes; ++i) {
        iplApplyBinauralEffect(effect, inbuffer, IPLVector3{1.0f, 1.0f, 1.0f}, IPL_HRTFINTERPOLATION_NEAREST, outbuffer);
        std::copy(std::begin(outputaudioframe), std::end(outputaudioframe), std::back_inserter(outputaudio));
        inbuffer.interleavedBuffer += framesize;
    }
    iplDestroyBinauralEffect(&effect);
    iplDestroyBinauralRenderer(&renderer);
    iplDestroyContext(&context);
    iplCleanup();
    save_output_audio("resources/outputaudio.raw", outputaudio);
    return 0;
}
