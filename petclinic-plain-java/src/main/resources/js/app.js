addEventListener("load", (event) => {
    let main = document.getElementsByTagName("main")[0];
    let nav = document.getElementsByClassName("nav");

    function displayPath(path) {
        if (path === '/owners/find') {
            main.innerHTML = `
                <h3>Find Owner</h3>
                <form action="/owners" method="get">
                <label for="lastName">Last name</label>
                <input id="lastName" type="text" name="lastName">
                <br>
                <button>Find Owner</button>
                </form>
                <a class="btn" href="/owners/new">Add Owner</a>
            `;
        } else if (path === "/owners") {
            main.innerHTML = `
                <h3>Owners</h3>
                loading...
            `;

            fetch(location.href, {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                }
            })
            .then((response) => response.json())
            .then((data) => {
                let table = "<table><thead><tr><th>Name</th><th>Address</th><th>City</th><th>Telephone</th><th>Pets</th></tr></thead>\n<tbody>\n";
                data.forEach((row) => {
                    let pets = row.pets.map((pet) => pet.name).join(', ');
                    table = table + `<tr>
                        <td><a href="/owners/${row.id}">${row.firstName} ${row.lastName}</a></td>
                        <td>${row.address}</td>
                        <td>${row.city}</td>
                        <td>${row.telephone}</td>
                        <td>${pets}</td>
                        </tr>\n`;
                });
                table = table + "</tbody></table>";
                main.innerHTML = `
                    <h3>Owners</h3>
                    <br>
                    ${table}
                `;
            });
        } else if (path.startsWith("/owners") && path.match(/^\/owners\/(\d+)$/)) {
            let ownerId = path.match(/^\/owners\/(\d+)$/)[1];
            main.innerHTML = `<h3>Owner Information: ${ownerId}</h3>loading...`

            fetch(location.href, {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                }
            })
            .then((response) => response.json())
            .then((data) => {
                let pets = "<table><tbody>";
                for (const pet of data.pets) {
                    pets = pets + `
                        <tr><td>
                            <dl>
                                <dt>Name</dt>
                                <dd>${pet.name}</dd>
                                <dt>Birth Date</dt>
                                <dd>${pet.birthDate}</dd>
                                <dt>Type</dt>
                                <dd>${pet.type}</dd>
                            </dl>
                        </td></tr>
                    `;
                }
                pets = pets + "</tbody></table>";

                main.innerHTML = `
                    <h3>Owner Information</h3>
                    <table><tbody>
                    <tr><th>Name</th><th>${data.firstName} ${data.lastName}</th></tr>
                    <tr><th>Address</th><td>${data.address}</td></tr>
                    <tr><th>City</th><td>${data.city}</td></tr>
                    <tr><th>Telephone</th><td>${data.telephone}</td></tr>
                    </tbod></table>
                    <a class="btn" href="/owners/${data.id}/edit">Edit Owner</a>
                    <a class="btn" href="/owners/${data.id}/pets/new">Add New Pet</a>

                    <h3>Pets and Visits</h3>
                    ${pets}
                `;
            });
        } else if (path === "/owners/new") {
            main.innerHTML = `
                <h3>Owner</h3>
                <form method="post">
                <label for="firstName">First Name</label>
                <input id="firstName" name="firstName" type="text" value="">
                <label for="lastName">Last Name</label>
                <input id="lastName" name="lastName" type="text" value="">
                <label for="address">Address</label>
                <input id="address" name="address" type="text" value="">
                <label for="city">City</label>
                <input id="city" name="city" type="text" value="">
                <label for="telephone">Telephone</label>
                <input id="telephone" name="telephone" type="text" value="">
                <button>Add Owner</button>
                </form>
            `;
        } else if (path.startsWith("/owners") && path.match(/^\/owners\/(\d+)\/edit$/)) {
            let ownerId = path.match(/^\/owners\/(\d+)\/edit$/)[1];
            main.innerHTML = `<h3>Owner: ${ownerId}</h3>loading...`

            fetch(`/owners/${ownerId}`, {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                }
            })
            .then((response) => response.json())
            .then((data) => {
                main.innerHTML = `
                    <h3>Owner</h3>
                    <form method="post">
                    <label for="firstName">First Name</label>
                    <input id="firstName" name="firstName" type="text" value="${data.firstName}">
                    <label for="lastName">Last Name</label>
                    <input id="lastName" name="lastName" type="text" value="${data.lastName}">
                    <label for="address">Address</label>
                    <input id="address" name="address" type="text" value="${data.address}">
                    <label for="city">City</label>
                    <input id="city" name="city" type="text" value="${data.city}">
                    <label for="telephone">Telephone</label>
                    <input id="telephone" name="telephone" type="text" value="${data.telephone}">
                    <button>Update Owner</button>
                    </form>
                `;
            });
        } else if (path === "/vets") {
            main.innerHTML = `
                <h3>Veterinarians</h3>
            `;
        } else if (path === "/") {
            main.innerHTML = `
                <h3>Home</h3>
                Welcome...
            `;
        } else {
            main.innerHTML = `
                <h3>Unknown Path: ${path}</h3>
            `
        }

        Array.prototype.forEach.call(nav, (element) => {
            element.classList.remove("active");
        });
        Array.prototype.filter.call(nav, (element) => {
            return new URL(element.href).pathname === path;
        }).forEach((element) => {
            element.classList.add("active");
        });
    }

    window.addEventListener('click', (event) => {
        if (event.target.tagName === 'A' && event.target.origin === location.origin) {
            event.preventDefault();
            let path = new URL(event.target.href).pathname;
            history.pushState(path, "", event.target.href);
            displayPath(path);
        }
    });
    window.addEventListener('popstate', (event) => {
        displayPath(event.state || "/");
    });
    window.addEventListener("submit", (event) => {
        if (event.target.method === "get") {
            event.preventDefault();

            let action = new URL(event.target.action);
            let path = action.pathname;

            let searchParams = new URLSearchParams();
            Array.prototype.forEach.call(event.target.elements, (control) => {
                if (control.name) {
                    searchParams.append(control.name, control.value);
                }
            });

            action.search = '?' + searchParams.toString();

            history.pushState(path, "", action.href);
            displayPath(path);
        } else if (event.target.method === "post") {
            event.preventDefault();

            let action = new URL(event.target.action);
            let data = {};
            Array.prototype.forEach.call(event.target.elements, (control) => {
                if (control.name) {
                    data[control.name] = control.value;
                }
            });

            fetch(action.href, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(data)
            })
            .then((response) => {
                let path = action.pathname;

                if (response.headers.has("X-Redirect-Path")) {
                    path = response.headers.get("X-Redirect-Path");
                    action.pathname = path;
                }

                history.pushState(path, "", action.href);
                displayPath(path);
            });
        }
    });

    displayPath(new URL(location.href).pathname);
});
