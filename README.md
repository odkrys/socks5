![Screenshot_20250725_122119_Socks5](https://github.com/user-attachments/assets/d7ab9593-6aac-4b4c-aa5d-24e09572220f)
![Screenshot_20250725_122129_Socks5](https://github.com/user-attachments/assets/f199033d-16cb-401a-893e-92e0d4513e18)


# Socks5

[![status](https://github.com/heiher/socks5/actions/workflows/build.yaml/badge.svg?branch=main&event=push)](https://github.com/heiher/socks5)

A simple and lightweight socks5 server for Android.

## Features

* IPv4/IPv6. (dual stack)
* Standard `CONNECT` command.
* Standard `UDP ASSOCIATE` command.
* Extended `FWD UDP` command. (UDP in TCP)
* Multiple username/password authentication.

## How to Build

Fork this project and create a new release, or build manually:

```bash
git clone --recursive https://github.com/heiher/socks5
cd socks5
gradle assembleDebug
```

## Dependencies

* HevSocks5Server - https://github.com/heiher/hev-socks5-server

## Contributors

* **hev** - https://hev.cc

## License

MIT
