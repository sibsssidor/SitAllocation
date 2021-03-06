# SitAllocation

The code is a possible solution for an optimal sit allocation problem on an airplane. It is part of a coding exercise and not meant for production, however it shows a way of solving the problem.

The problem has been structured as an integer linear programming (ILP) problem and solved using an ILP open source solver. This type of approach can look over complicated at a glance but offers better flexibility than an ad-hoc heuristic, it takes advantage of existing libraries and is more open to changes in business logic (e.g. we can introduce rows of different size, we don’t care if we exceed the plane capacity, we can consider more unsatisfied the users flying than the ones left behind in case of overbooking etc.). 
The overall idea is to assign as many groups as possible according to the business goals and after that fill the flight with unsatisfied users. If the number of users is greater than the sits on the flight the problem does not change, the linear solver will try to maximize the score.
See ODT/PDF for more information.
