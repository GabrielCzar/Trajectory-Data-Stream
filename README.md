# Trajectory Data Stream

#### Dataset
- [Beijing OSM](https://download.bbbike.org/osm/bbbike/Beijing/) has info about the city
- [Beijing Poly](https://download.bbbike.org/osm/bbbike/Beijing/Beijing.poly) has info about outermost nodes
- [Taxi Data](http://research.microsoft.com/apps/pubs/?id=152883) Microsoft has made available GPS data from 10,357 taxis in Beijing. Each taxi's location is sampled every 177 seconds on average and we're given a week's worth of data.

The data are given in .txt files in the following format
1.txt:  
```
  1,2008-02-02 15:36:08,116.51172,39.92123
  1,2008-02-02 15:46:08,116.51135,39.93883
  1,2008-02-02 15:46:08,116.51135,39.93883
```

#### Scripts

- [Create CSV file with taxi data](scripts/gerar_csv.py)
