class BankCustomer:
    def __init__(self, id, name, email, accn, srtc, add1, add2, town, pc):
        self.id = id
        self.name = name
        self.emailAddress = email
        self.accountNumber = accn
        self.sortCode = srtc
        self.address1 = add1
        self.address2 = add2
        self.town = town
        self.postCode = pc

    def __eq__(self, other):
        return self.id == other.id

    def __hash__(self):
        return hash(self.id)

    def __repr__(self):
        return self.__str__()

    def __str__(self):
        format_string = "----------\n{0:d}\n{1}\nAccount: {2:d} ({3})\n{4}, {5}, {6}, {7}\n----------"
        strt = str(self.sortCode)
        strt = "{0}-{1}-{2}".format(strt[0:2],strt[2:4],strt[4:6])
        return format_string.format(self.id, self.name, self.accountNumber, strt,
                                    self.address1, self.address2, self.town, self.postCode)
