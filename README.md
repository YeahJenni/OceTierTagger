# TiersPlus!
TiersPlus is the BETTER TierTagger! (Subject to user opinion-). Featuring mctiers.com, mctiers.io, ocetiers.net and more to come, this TierTagger also has beautiful icons!
All the code is available here, with the API being closed source, however reachable at api.yeahjenni.xyz.

For developers:
Requests to api.yeahjenni.xyz should follow the format below:

api.yeahjenni.xyz/ocetiers/player/yeahjenni

```api.yeahjenni.xyz``` - API endpoint, ```dev.yeahjenni.xyz``` can also be used  
```/ocetiers``` - The tierlist to be used. Current options are: `ocetiers` for ocetiers.net, `mctiers` for mctiers.com, and `mctiersio` for mctiers.io  
```/player``` - Just defines that a player is being searched up  
```/yeahjenni``` - The player being looked up, whoevers IGN would go here. Below is the data the API would return:  

```
{
  "gameModes": {
    "axe": {
      "isLT": true,
      "tier": "LT5"
    },
    "cart": {
      "isLT": true,
      "tier": "LT5"
    },
    "crystal": {
      "isLT": false,
      "tier": "HT4"
    },
    "diamondPot": {
      "isLT": true,
      "tier": "LT5"
    },
    "diamondSmp": {
      "isLT": true,
      "tier": "LT5"
    },
    "mace": {
      "isLT": true,
      "tier": "LT5"
    },
    "netheritePot": {
      "isLT": true,
      "tier": "LT5"
    },
    "smp": {
      "isLT": true,
      "tier": "LT5"
    },
    "sword": {
      "isLT": true,
      "tier": "LT5"
    },
    "uhc": {
      "isLT": true,
      "tier": "LT5"
    }
  },
  "id": "1047076637759504415",
  "leaderboardPosition": 467,
  "minecraft_uuid": "90858040-ce55-4568-b60f-d928c9dc1822",
  "oceaniasStaff": false,
  "owner": true,
  "ranked": true,
  "score": 4,
  "tier": "LT5",
  "username": "YeahJenni"
}
```

