# Lobstar

> [!IMPORTANT]
> Lobstar is currently in active development and **not ready for production use**. Expect frequent breaking changes and incomplete features.

## Overview

**Lobstar** is an advanced whitelisting and connection management system for Minecraft networks using the **Velocity**
proxy. It provides a modern, easy-to-use web dashboard that empowers server admins to manage players, servers,
whitelists, and virtual hosts with ease.

Whether you're running a large network or a small private setup, Lobstar offers powerful tools to keep your server
infrastructure organized and secure.

## Features

- **Dynamic Server Registration** Add or remove servers at runtime, no proxy restart needed.
- **Flexible Whitelisting** Manage global and per-server whitelists.
- **Virtual Hosts** Route players to specific servers based on the domain they join from.

## Installation

Lobstar is a multi-component system, requiring several services to run.

### Requirements

- [Docker & Docker Compose](https://docs.docker.com/compose/)
- [Velocity Proxy](https://velocitypowered.com/)
- (Recommended) Linux or Unix-based system

### Components

- [**Lobstar API**](https://github.com/DevPieter/lobstar_api)
- [**Lobstar Panel (Web Dashboard)**](https://github.com/DevPieter/lobstar_panel)
- [**Velocity Plugin**](https://github.com/DevPieter/Lobstar-v2)
- **PostgreSQL** – Core database
- **Redis** *(planned)* – For caching and messaging

### Setup Guide

_Installation instructions are coming soon&trade;_  
We are working on a full setup guide including Docker Compose templates and usage examples.

## Preview

### Player

<img src="https://raw.githubusercontent.com/DevPieter/Lobstar-v2/main/docs/images/player-1.png?raw=true" width="1920" height="917"></img>

### Server

<img src="https://raw.githubusercontent.com/DevPieter/Lobstar-v2/main/docs/images/server-1.png?raw=true" width="1920" height="917"></img>

<img src="https://raw.githubusercontent.com/DevPieter/Lobstar-v2/main/docs/images/server-2.png?raw=true" width="512" height="662"></img>

<img src="https://raw.githubusercontent.com/DevPieter/Lobstar-v2/main/docs/images/server-3.png?raw=true" width="512" height="552"></img>

### Virtual Host

<img src="https://raw.githubusercontent.com/DevPieter/Lobstar-v2/main/docs/images/virtual-host-1.png?raw=true" width="1920" height="917"></img>

<img src="https://raw.githubusercontent.com/DevPieter/Lobstar-v2/main/docs/images/virtual-host-1.png?raw=true" width="512" height="538"></img>

## Roadmap

- ✅ Core whitelist and server management
- ✅ Dynamic server registration
- ✅ Virtual host routing
- ✅ Web dashboard integration
- 🔄 Redis-based caching and messaging
- 🔄 Admin roles and permissions
- 🔄 Public API documentation
- 🔄 Metrics and alerts