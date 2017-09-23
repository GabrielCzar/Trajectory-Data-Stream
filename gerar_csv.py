import pandas as pd
import numpy as np
import os 

HEADER = ['taxi_id', 'date_time', 'longitude', 'latitude']

def get_files(path):
	all_files = []
	for root, dirs, files in os.walk(path):
		for name in files:
			all_files.append(os.path.join(root, name))
	return all_files

def get_data_from_folder(path):
	global HEADER 
	return pd.read_csv(path, infer_datetime_format = True, header = None, parse_dates = [1], names = HEADER)

data = []
file = 'path_dataset'

for _, file_path in enumerate(get_files(file)):
	data.append(get_data_from_folder(file_path))

data = pd.concat(data, ignore_index=True)

data.to_csv('taxi_data.csv', index=False)
