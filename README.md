# Emercast Backend

This is the central component of the emercast system. It allows authorities to manage other authorities and to create emergency notifications. It is used by the frontend and android application component.

# Build

Run ```docker build .``` to build a containerized version of the backend.

# Run

The backend requires the following environment variables to run:

- **emercast.cors.allowed.url**: <br>The url under which the frontend is reachable. Use ```http://localhost:3000``` for a local deployment
- **emercast.jwt.private-key**: <br>A random string used for encrypting the jwt tokens. Should be at least 64 characters long
- **spring.data.mongodb.uri**: <br>The mongodb connection string. Must point to a reachable mongodb instance
