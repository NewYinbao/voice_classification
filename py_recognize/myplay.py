from nybrecord import paly_pcm, read_data_from_file
import os
import matplotlib.pyplot as plt


if __name__ == "__main__":
    for filename in os.listdir('test/'):
        paly_pcm('test/' + filename )
        print(filename[:2])
        data = read_data_from_file('test/' + filename, 32000)
        plt.plot(range(len(data)), data)
        plt.show()
