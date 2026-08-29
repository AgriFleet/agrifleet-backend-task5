# AG-18 Technical Notes

## User story

As a machinery operator, I want the system to determine an efficient sequence
for visiting multiple assigned farms so that total travel distance and fuel
consumption are minimized.

## Scope boundary

AG-18 receives farms that have already been assigned/selected. It decides their
visit order. AG-19 selects valuable bookings and AG-20 performs Genetic
Algorithm optimization, so neither is duplicated here.

## Mathematical model

Given depot `0`, farms `1..n`, and distance `d(i,j)`, find a permutation `p`
that minimizes:

`d(0,p1) + sum(d(pk, p(k+1))) + d(pn,0)`

The last term is omitted for an open route. With constant fuel rate `r`, fuel is
`totalDistance * r`; therefore minimizing distance also minimizes travel fuel.

## Held-Karp state and recurrence

`DP[S][j]` is the minimum distance from the depot, through exactly the farms in
subset `S`, ending at farm `j`.

Base case:

`DP[{j}][j] = d(0,j)`

Transition:

`DP[S][j] = min(DP[S-{j}][k] + d(k,j))` for every `k` in `S-{j}`.

Final cycle:

`min(DP[ALL][j] + d(j,0))`

The `parent` table stores the predecessor that produced each best state. It is
used to reconstruct the actual farm order rather than returning only a cost.

## Pseudocode

```text
OPTIMIZE_VISIT_SEQUENCE(distance, returnToDepot)
    n <- number of farms
    if n > 18
        return NEAREST_NEIGHBOUR(distance, returnToDepot)

    fill DP and PARENT with infinity and -1
    for each farm j
        DP[bit(j)][j] <- distance[depot][j]

    for each subset S
        for each last farm j in S
            previousSet <- S without j
            for each farm k in previousSet
                candidate <- DP[previousSet][k] + distance[k][j]
                if candidate < DP[S][j]
                    DP[S][j] <- candidate
                    PARENT[S][j] <- k

    choose the cheapest final farm
    add return-to-depot cost when required
    backtrack through PARENT to construct the sequence
    return sequence, distance and estimated fuel
```

## Complexity derivation

There are `2^n` subsets and at most `n` possible last farms per subset. Each
state considers up to `n` predecessors. Therefore:

- Time: Theta(`n^2 * 2^n`) for non-trivial exact inputs.
- Space: Theta(`n * 2^n`) for the DP and parent tables.
- Best/average/worst asymptotic time are the same because the DP systematically
  evaluates its state space.

For more than 18 farms, nearest neighbour performs `n + (n-1) + ... + 1`
comparisons:

- Time: Theta(`n^2`).
- Space: Theta(`n`).
- It is fast but does not guarantee a globally optimal route.

## Suggested acceptance criteria

1. Every assigned farm appears exactly once in the optimized farm order.
2. The depot is the first location and, for round trips, the final location.
3. The API returns total distance and estimated fuel consumption.
4. A Task 1 road-distance matrix is used when supplied.
5. Coordinates are used to calculate Haversine distances when no matrix exists.
6. Up to 18 farms receive an exact optimum with Held-Karp DP.
7. Larger inputs return a finite heuristic solution without exponential memory.
8. Invalid coordinates, duplicate IDs and malformed matrices return HTTP 400.

## Test evidence included

- Known four-location TSP: exact tour cost is 80 km.
- Open-route case: depot -> A -> B costs 5 km without a depot return.
- Fuel calculation: 80 km at 0.25 L/km returns 20 L.
- Route contains depot, all farms once, and depot return.
- Duplicate location IDs are rejected.

## Viva points

- This is a single-vehicle TSP variant, not a full multi-vehicle VRP.
- Held-Karp was chosen instead of brute force because `n^2 * 2^n` grows much
  slower than `n!`, while still guaranteeing optimality.
- It is intentionally not used for large inputs because exponential memory is
  still impractical.
- Haversine is a fallback estimate. A road-distance matrix from Task 1 is more
  operationally accurate.
- Constant fuel rate makes distance and fuel objectives equivalent. Variable
  terrain/load rates would require edge-specific fuel costs or a multi-objective
  cost function.
