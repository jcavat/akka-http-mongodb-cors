# Akka-http snippet with OAuth2 Bearer + CORS + MongoDB

## Summary

This snippet is based on [https://github.com/jcavat/akka-http-with-bearer-tokens](https://github.com/jcavat/akka-http-with-bearer-tokens)

We use OAuth2 baerer to authenticate + CORS + MongoDB database to persist data

The list of credentials are given in the `application.conf` file in `src/main/resources` folder. MongoDB credentials 
must be those declared in `docker-compose.yml` file.

The documents to get/post/persist are defined as case classes : 

```
final case class User(_id: Option[ObjectId], name: String, email: String, tags: List[Tag])
final case class Tag(name: String)
```

Run the server with `sbt run` and try:

post some users :

```
curl -H "Content-Type: application/json" -H "Authorization: Bearer ABCD" http://localhost:8080/users -X POST -d '{"name": "asdf", "email": "adsf@test.com", "tags": [{"name": "youpie"}, {"name": "haha"}]}'
curl -H "Content-Type: application/json" -H "Authorization: Bearer ABCD" http://localhost:8080/users -X POST -d '{"name": "tutu", "email": "tutu@tutu.com", "tags": []}'
```

get all users:

```
curl -H "Content-Type: application/json" -H "Authorization: Bearer ABCD" http://localhost:8080/users
```
