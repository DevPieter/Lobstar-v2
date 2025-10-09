# Lobstar

> [!IMPORTANT]
> Lobstar is currently in active development and **not ready for production use**. Expect frequent breaking changes and incomplete features.

## Overview

**Lobstar** is an advanced whitelisting and connection management system for Minecraft networks using the **Velocity**
proxy. It provides a modern, easy-to-use web dashboard that helps server admins to manage players, servers,
whitelists, and virtual hosts with ease.

Whether you're running a large network or a small private setup, Lobstar aims to simplify server and player management.

## Features

- **Dynamic Server Registration** Add or remove servers at runtime, no proxy restart needed.
- **Flexible Whitelisting** Manage global and per-server whitelists.
- **Virtual Hosts** Route players to specific servers based on the domain they join from.

## Installation

Lobstar is a multi-component system, requiring several services to run. Below are the main components and their requirements. Lobstar is designed to be deployed using Docker Compose for ease of deployment.

### Requirements

- (Recommended) Linux-based server with at least 1GB[^1] of RAM
- [Docker & Docker Compose](https://docs.docker.com/compose/)
- [Velocity Proxy](https://velocitypowered.com/)

[^1]: The memory requirement may vary based on the size of your server network, number of players, and the state of the project.

### Components

- **Lobstar Server** ([API](https://github.com/DevPieter/lobstar_api) & [Panel](https://github.com/DevPieter/lobstar_panel))
    - The Lobstar Server combines the backend API and web dashboard for easier deployment.
    - It handles all core logic, manages Velocity interactions, and serves the web interface.
- **Velocity Plugin** ([Plugin](https://github.com/DevPieter/Lobstar-v2))
    - The plugin handles communication between the Velocity proxy and the Lobstar API.
    - It manages player connections, whitelisting, server routing, and other related features.
- **PostgreSQL**
    - Stores all persistent data.
- **Redis**
    - Used primarily for caching server and player states to reduce database load and improve performance.

### Setup Guide

_Installation instructions are coming soon&trade;_  
We are working on a full setup guide including Docker Compose templates and usage examples.

## Preview

### Player

<img src="https://github.com/DevPieter/Lobstar-v2/blob/main/docs/images/player-1.png?raw=true" width="1920" height="917"></img>

### Server

<img src="https://github.com/DevPieter/Lobstar-v2/blob/main/docs/images/server-1.png?raw=true" width="1920" height="917"></img>

<img src="https://github.com/DevPieter/Lobstar-v2/blob/main/docs/images/server-2.png?raw=true" width="512" height="662"></img>

<img src="https://github.com/DevPieter/Lobstar-v2/blob/main/docs/images/server-3.png?raw=true" width="512" height="552"></img>

### Virtual Host

<img src="https://github.com/DevPieter/Lobstar-v2/blob/main/docs/images/virtual-host-1.png?raw=true" width="1920" height="917"></img>

<img src="https://github.com/DevPieter/Lobstar-v2/blob/main/docs/images/virtual-host-2.png?raw=true" width="512" height="538"></img>

## Roadmap

- ✅ Core whitelist and server management
- ✅ Dynamic server registration
- ✅ Virtual host routing
- ✅ Web dashboard integration
- ✅ Redis-based caching
- ✅ Admin roles and permissions
- 🔄 API documentation
- 🔄 Metrics and alerts
